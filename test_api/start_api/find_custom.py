import json
from flask_restful import Resource, reqparse
import psycopg2
from config import host, user, password_db, db_name

# Подключение к базе данных
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

class GetCustom(Resource):
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument("name_lecture", type=str, required=False, default=None)
        parser.add_argument("object_name", type=str, required=False, default=None)
        parser.add_argument("course", type=int, required=False, default=None)
        parser.add_argument("popular", type=int, required=False, default=None)
        args = parser.parse_args()

        try:
            with connection.cursor() as cursor:
                query = """
                    SELECT lecture.name, object.object_name, lecture.course, lecture.count_view
                    FROM lecture 
                    INNER JOIN object ON lecture.object_id = object.id_object
                """
                conditions = []
                params = {}

                if args['name_lecture']:
                    conditions.append("lecture.name LIKE %(name_lecture)s")
                    params['name_lecture'] = f"%{args['name_lecture']}%"

                if args['object_name']:
                    conditions.append("object.object_name = %(object_name)s")
                    params['object_name'] = args['object_name']

                if args['course'] is not None:
                    conditions.append("lecture.course = %(course)s")
                    params['course'] = args['course']

                if conditions:
                    query += " WHERE " + " AND ".join(conditions)

                if args['popular'] is not None:
                    if args['popular'] == 0:
                        query += " ORDER BY lecture.count_view DESC"
                    elif args['popular'] == 1:
                        query += " ORDER BY lecture.count_view ASC"

                cursor.execute(query, params)
                rows = cursor.fetchall()
                columns = [desc[0] for desc in cursor.description]
                result = [dict(zip(columns, row)) for row in rows]

                return json.loads(json.dumps(result, ensure_ascii=False)), 200
        except Exception as e:
            return {"message": f"Ошибка при получении данных: {e}"}, 500