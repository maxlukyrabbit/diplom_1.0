from flask_restful import Resource, reqparse
from config import host, user, password_db, db_name
import psycopg2
import json

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


class FindAll(Resource):
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument("surname", type=int, required=True, help="Course is required")
        parser.add_argument("name", type=str, required=True, help="Password is required")
        args = parser.parse_args()
        try:
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    SELECT surname
                    FROM "user" 
                    INNER JOIN object ON lecture.object_id = object.id_object
                    """
                )

                rows = cursor.fetchall()
                columns = [desc[0] for desc in cursor.description]
                result = [dict(zip(columns, row)) for row in rows]
                json_result = json.dumps(result, ensure_ascii=False, indent=4)

            return json.loads(json_result), 200
        except Exception as e:
            return {"message": f"Error retrieving data: {e}"}, 500
