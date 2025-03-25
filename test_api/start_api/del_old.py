import json
from flask import request
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
class del_old(Resource):
    def get(self):
        try:
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    DELETE FROM lecture
                    WHERE count_view = (SELECT MIN(count_view) FROM lecture);
                    """
                )
            return 200
        except Exception as e:
            return {"message": f"Error retrieving data: {e}"}, 500

