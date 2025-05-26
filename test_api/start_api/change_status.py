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

            if not all([status, id_lecture]):
                return {"message": "Missing required parameters"}, 400

            try:
                status = int(status)
                id_lecture = int(id_lecture)
            except ValueError:
                return {"message": "Invalid parameter type"}, 400

            with connection.cursor() as cursor:
                if status != 2:
                    cursor.execute(
                        '''
                        UPDATE "lecture"
                        SET status = %s
                        WHERE id_lecture = %s
                        ''',
                        (status, id_lecture)
                    )
                else:
                    cursor.execute(
                        '''
                        DELETE FROM "lecture"
                        WHERE id_lecture = %s
                        ''',
                        (id_lecture,)
                    )

                connection.commit()

            return {"message": "Operation successful"}, 200

        except Exception as e:
            connection.rollback()
            return {"message": f"Error processing request: {str(e)}"}, 500
