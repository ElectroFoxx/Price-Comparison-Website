import os
from google.cloud.sql.connector import Connector
import sqlalchemy
from flask import Flask, request, jsonify
from playwright.sync_api import sync_playwright


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
            ip_type="PRIVATE",
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


@app.route("/", methods=["POST", "PUT"])
def main():
    if request.method == "POST":
        data = request.get_json(silent=True)

        if not data:
            return jsonify({"error": "No JSON data provided"}), 400

        manufacturer_code = data.get("manufacturer_code")

        pool = get_pool()

        with pool.connect() as db_conn:
            result = db_conn.execute(
                "INSERT INTO products (manufacturer_code) VALUES (%s) RETURNING *",
                manufacturer_code,
            )
            inserted_id = result.fetchone()[0]
            db_conn.commit()

        data = scrape(manufacturer_code)

        with pool.connect() as db_conn:
            db_conn.execute(
                "INSERT INTO prices (product_id, price_xkom, price_morele, price_media_expert) VALUES (%d, %d, %d, %d)",
                inserted_id,
                data["x-kom"],
                data["morele"],
                data["media_expert"]
            )
            db_conn.commit()
        
        return 'OK'

    if request.method == "PUT":
        pass


if __name__ == "__main__":
    from waitress import serve

    serve(app, host="0.0.0.0", port=int(os.environ.get("PORT", 8080)))
