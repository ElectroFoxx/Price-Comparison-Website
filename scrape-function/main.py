#     with pool.connect() as db_conn:
#         results = db_conn.execute(sqlalchemy.text("SELECT schemaname, tablename FROM pg_tables;")).fetchall()

#         for row in results:
#             print(row)

#     return "OK"

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


app = Flask(__name__)


@app.route("/")
def scrape():
    data = request.get_json(silent=True)

    if not data:
        return jsonify({"error": "No JSON data provided"}), 400

    manufacturer_code = data.get("manufacturer_code")

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
        page.goto(f"{x_kom_link}{manufacturer_code}", wait_until="domcontentloaded")

        # page.wait_for_selector("span.parts__Price-sc-6e255ce0-0")
        price = page.query_selector("span.sc-jnqLxu").inner_text()
        return price


if __name__ == "__main__":
    from waitress import serve

    serve(app, host="0.0.0.0", port=int(os.environ.get("PORT", 8080)))
