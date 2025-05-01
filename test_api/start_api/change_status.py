from flask import request
from flask_restful import Resource
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


class changeStatus(Resource):
    def get(self):
        try:
            status = request.args.get("status")
            id_lecture = request.args.get("id_lecture")
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    UPDATE "lecture"
                    SET status = %s
                    WHERE id_lecture = %s;
                    """,
                    (status, id_lecture)
                )
                connection.commit()
            return 200
        except Exception as e:
            return {"message": f"Error retrieving data: {e}"}, 500


    