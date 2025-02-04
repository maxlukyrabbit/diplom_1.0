import base64
from flask_restful import Resource, reqparse
from config import host, user, password_db, db_name
import psycopg2

try:
    connection = psycopg2.connect(
        host=host,
        user=user,
        password=password_db,
        database=db_name
    )
    connection.autocommit = True
except Exception as e:
    print(f"Ошибка подключения к базе данных: {e}")
    exit()

class AddLecture(Resource):
    def put(self):
        parser = reqparse.RequestParser()
        parser.add_argument("name", type=str, required=True, help="Name is required")
        parser.add_argument("object_id", type=int, required=True, help="Object ID is required")
        parser.add_argument("file_pdf", type=str, required=True, help="File PDF is required")
        parser.add_argument("course", type=int, required=True, help="Course is required")
        parser.add_argument("user_id", type=int, required=True, help="User ID is required")
        parser.add_argument("count_view", type=int, required=True, help="Count view is required")
        args = parser.parse_args()

        try:
            file_pdf_bytes = base64.b64decode(args["file_pdf"])

            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT INTO lecture (
                        name, object_id, file_pdf, course, user_id, count_view
                    )
                    VALUES (%s, %s, %s, %s, %s, %s)
                    """,
                    (
                        args["name"], args["object_id"], file_pdf_bytes,
                        args["course"], args["user_id"],
                        args["count_view"]
                    )
                )
            return {"message": "Lecture added successfully"}, 201
        except Exception as e:
            return {"message": f"Error adding lecture: {e}"}, 500
