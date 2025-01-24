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

class AddUser(Resource):
    def put(self):
        # Используем reqparse для извлечения данных из запроса
        parser = reqparse.RequestParser()
        parser.add_argument("surname", type=str, required=True, help="Surname is required")
        parser.add_argument("name", type=str, required=True, help="Name is required")
        parser.add_argument("patronymic", type=str, required=True, help="Patronymic is required")
        parser.add_argument("type_user_id", type=int, required=True, help="Type User ID is required")
        parser.add_argument("specialization_id", type=int, required=True, help="Specialization ID is required")
        parser.add_argument("course", type=int, required=True, help="Course is required")
        parser.add_argument("password", type=str, required=True, help="Password is required")
        args = parser.parse_args()

        try:
            # Выполнение SQL-запроса
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT INTO "user" (
                        surname, name, patronymic, type_user_id, specialization_id, course, password
                    )
                    VALUES (%s, %s, %s, %s, %s, %s, %s)
                    """,
                    (
                        args["surname"], args["name"], args["patronymic"],
                        args["type_user_id"], args["specialization_id"],
                        args["course"], args["password"]
                    )
                )
            return {"message": "User added successfully"}, 201
        except Exception as e:
            return {"message": f"Error adding user: {e}"}, 500
