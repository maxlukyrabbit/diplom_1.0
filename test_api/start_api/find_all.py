from flask_restful import Resource
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
        try:
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    SELECT name, object_id, course FROM lecture
                    """
                )
                rows = cursor.fetchall()
                columns = [desc[0] for desc in cursor.description]
                result = [dict(zip(columns, row)) for row in rows]
                json_result = json.dumps(result, ensure_ascii=False, indent=4)

            return json.loads(json_result), 200
        except Exception as e:
            return {"message": f"Error retrieving data: {e}"}, 500




































# from flask_restful import Resource, reqparse
# from config import host, user, password_db, db_name
# import psycopg2
# import json
# import base64
#
# try:
#     connection = psycopg2.connect(
#         host=host,
#         user=user,
#         password=password_db,
#         database=db_name
#     )
#     connection.autocommit = True
# except Exception as e:
#     print(f"Ошибка подключения к базе данных: {e}")
#     exit()
#
#
# class FindAll(Resource):
#     def get(self):
#         try:
#             with connection.cursor() as cursor:
#                 cursor.execute(
#                     """
#                     SELECT * FROM lecture
#                     """
#                 )
#                 rows = cursor.fetchall()
#                 columns = [desc[0] for desc in cursor.description]
#
#                 result = []
#                 for row in rows:
#                     row_dict = dict(zip(columns, row))
#
#                     if row_dict.get('file_pdf'):
#                         row_dict['file_pdf'] = base64.b64encode(row_dict['file_pdf']).decode('utf-8')
#
#                     result.append(row_dict)
#
#                 json_result = json.dumps(result, ensure_ascii=False, indent=4)
#
#             return json_result, 200
#         except Exception as e:
#             return {"message": f"Error retrieving lectures: {e}"}, 500
