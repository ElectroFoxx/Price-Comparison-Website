from datetime import datetime, timedelta
import os
from google.cloud.sql.connector import Connector
import sqlalchemy
from flask import Flask, request, jsonify
from playwright.sync_api import sync_playwright
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail


def get_pool():
    instance_connection_name = os.environ["INSTANCE_CONNECTION_NAME"]
    db_user = os.environ["DB_USER"]
    db_pass = os.environ["DB_PASS"]
    db_name = os.environ["DB_NAME"]

    connector = Connector()

    def getconn():
        conn = connector.connect(
            instance_connection_name,
            "pg8000",
            user=db_user,
            password=db_pass,
            db=db_name,
        )
        return conn

    pool = sqlalchemy.create_engine("postgresql+pg8000://", creator=getconn)
    return pool


x_kom_link = "https://www.x-kom.pl/szukaj?q="
morele_link = "https://www.morele.net/wyszukiwarka/?q="
media_expert_link = (
    "https://www.mediaexpert.pl/search?query[menu_item]=&query[querystring]="
)

excluded_resource_types = ["stylesheet", "script", "image", "font"]


def block_aggressively(route):
    if route.request.resource_type != "document":
        route.abort()
    else:
        route.continue_()


def keep_only_numbers(s):
    return "".join(filter(str.isdigit, s))


app = Flask(__name__)


def scrape(manufacturer_code: str):
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(
            args=["--disable-extensions", "--disable-gpu", "--no-sandbox"],
        )

        context = browser.new_context(
            user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36",
            java_script_enabled=True,
            viewport={"width": 1920, "height": 1080},
        )

        page = context.new_page()

        page.route("**/*", block_aggressively)

        # check if x-kom website has an item

        try:
            page.goto(f"{x_kom_link}{manufacturer_code}", wait_until="domcontentloaded")

            price = 0
            not_found_element = page.query_selector("div.gGxjMd")
            if (
                not_found_element is not None
                and not_found_element.inner_text().startswith(
                    "Nie znaleźliśmy wyników dla"
                )
            ):
                price_xkom = price
            else:
                for span in page.query_selector_all("span.sc-jnqLxu"):
                    span_text = span.inner_text()
                    if span_text.endswith("zł"):
                        break
                price_xkom = int(
                    span_text.replace(" ", "").replace("zł", "").replace(",", "")
                )
        except:
            price_xkom = 0

        # check if morele website has an item

        try:
            page.goto(
                f"{morele_link}{manufacturer_code}", wait_until="domcontentloaded"
            )

            price = 0
            not_found_element = page.query_selector("h2")
            if (
                not_found_element is not None
                and not_found_element.inner_text().startswith("Brak wyników")
            ):
                price_morele = price
            else:
                div_text = page.query_selector("div.price-new").inner_text()

                if "," in div_text:
                    price = int(
                        div_text.replace(" ", "").replace("zł", "").replace(",", "")
                    )
                else:
                    price = int(div_text.replace(" ", "").replace("zł", "")) * 100

                price_morele = price
        except:
            price_morele = 0

        # check if media expert website has an item

        try:
            page.goto(
                f"{media_expert_link}{manufacturer_code}", wait_until="domcontentloaded"
            )

            price = 0
            not_found_element = page.query_selector("h2")
            if (
                not_found_element is not None
                and not_found_element.inner_text().startswith(
                    "Brak rezultatów wyszukiwania dla frazy"
                )
            ):
                price_media_expert = price
            else:
                whole = int(
                    keep_only_numbers(page.query_selector("span.whole").inner_text())
                )
                rest = int(page.query_selector("span.cents").inner_text())

                price_media_expert = whole * 100 + rest
        except:
            price_media_expert = 0

        data = {
            "x-kom": price_xkom,
            "morele": price_morele,
            "media_expert": price_media_expert,
        }

        return data
    
    
def insert_product(pool, manufacturer_code):
    insert_product = sqlalchemy.text(
        "INSERT INTO products (manufacturer_code, created_at, emailed_at) VALUES (:manufacturer_code, :created_at, :emailed_at) RETURNING *"
    )

    with pool.connect() as db_conn:
        result = db_conn.execute(
            insert_product,
            parameters={
                "manufacturer_code": manufacturer_code,
                "created_at": sqlalchemy.func.now(),
                "emailed_at": datetime.now() - timedelta(days=1)
            },
        )
        row = result.mappings().fetchone()
        db_conn.commit()
    
    return row['id']


def insert_price(pool, product_id, data):
    insert_price = sqlalchemy.text(
        "INSERT INTO prices (product_id, created_at, price_xkom, price_morele, price_media_expert) VALUES (:product_id, :created_at, :price_xkom, :price_morele, :price_media_expert)"
    )

    with pool.connect() as db_conn:
        db_conn.execute(
            insert_price,
            parameters={
                "product_id": product_id,
                "created_at": sqlalchemy.func.now(),
                "price_xkom": data["x-kom"],
                "price_morele": data["morele"],
                "price_media_expert": data["media_expert"],
            },
        )
        db_conn.commit()
        

def get_not_refreshed_products(pool, quantity=5):
    with pool.connect() as db_conn:
        query = sqlalchemy.text(f"""
            SELECT * 
            FROM products 
            WHERE id NOT IN (
                SELECT product_id
                FROM prices
                WHERE created_at >= CURRENT_DATE
            )
            LIMIT {quantity}
        """)
        result = db_conn.execute(query)
        rows = result.mappings().all()
        
        return rows
    
def get_not_refreshed_products(pool, quantity=5):
    with pool.connect() as db_conn:
        query = sqlalchemy.text(f"""
            SELECT * 
            FROM products 
            WHERE id NOT IN (
                SELECT product_id
                FROM prices
                WHERE created_at >= CURRENT_DATE
            )
            LIMIT {quantity}
        """)
        result = db_conn.execute(query)
        rows = result.mappings().all()
        
        return rows
    
def get_not_emailed_product(pool):
    with pool.connect() as db_conn:
        query = sqlalchemy.text(f"""
            SELECT * 
            FROM products 
            WHERE emailed_at < CURRENT_DATE
            LIMIT 1
        """)
        result = db_conn.execute(query)
        rows = result.mappings().all()
        
        return rows
    
def get_min_price(pool, product_id):
    with pool.connect() as db_conn:
        query = sqlalchemy.text(f"""
            SELECT *
            FROM prices 
            WHERE product_id = {product_id}
            AND created_at >= CURRENT_DATE
            LIMIT 1
        """)
        result = db_conn.execute(query)
        row = result.mappings().all()[0]
        
        price_media_expert = int(row['price_media_expert'])
        price_morele = int(row['price_morele'])
        price_xkom = int(row['price_xkom'])
        
        
        numbers = [price_media_expert, price_morele, price_xkom]
        numbers = [x for x in numbers if x != 0]
    
        if not numbers:
            return None
    
        return min(numbers)
    

def get_interested_users_emails(pool, product_id, lowest_price):
    with pool.connect() as db_conn:
        query = sqlalchemy.text(f"""
            SELECT u.email
            FROM subscriptions s
            JOIN users u
            ON u.id = s.user_id
            WHERE s.product_id = {product_id}
            AND s.price <= {lowest_price}
        """)
        result = db_conn.execute(query)
        rows = result.mappings().all()
        
        emails = [row['email'] for row in rows]
    
        return min(emails)


@app.route("/", methods=["POST", "PUT"])
def main():
    pool = get_pool()
    if request.method == "POST":
        data = request.get_json(silent=True)

        if not data:
            pool.dispose()
            return jsonify({"error": "No JSON data provided"}), 400

        manufacturer_code = data.get("manufacturer_code")
        inserted_id = insert_product(pool, manufacturer_code)

        data = scrape(manufacturer_code)
        insert_price(pool, inserted_id, data)
        pool.dispose()

        return str(inserted_id)

    if request.method == "PUT":
        rows = get_not_refreshed_products(pool)
        
        for row in rows:
            data = scrape(row['manufacturer_code'])
            insert_price(pool, row['id'], data)
        pool.dispose()
            
        return "OK" if len(rows) == 5 else "DONE"
    
    
@app.route("/email", methods=["PUT"])
def send():
    pool = get_pool()
    
    not_emailed_product = get_not_emailed_product(pool)
    
    if len(not_emailed_product) == 0:
        pool.dispose()
        return "DONE"
    product_id = not_emailed_product[0]['id']
    min_price = get_min_price(pool, product_id)
    emails = get_interested_users_emails(pool, product_id, min_price)
    pool.dispose()
    
    message = Mail(
    from_email='maciejd@student.agh.edu.pl',
    to_emails=emails,
    subject='Price Alert',
    html_content=f'Product {not_emailed_product['manufacturer_code']} at price {min_price}')
    try:
        sg = SendGridAPIClient(os.environ['SEND_GRID_API'])
        sg.send(message)
    except Exception as e:
        print(str(e))
    return "OK"



if __name__ == "__main__":
    from waitress import serve

    serve(app, host="0.0.0.0", port=int(os.environ.get("PORT", 8080)))
