from flask import Flask
from flask_restful import Api
from add_user import AddUser
from add_lecture import AddLecture
from find_most import FindMost
from find_all import FindAll
from get_specialization import GetSpecialization
from get_object import GetObject

app = Flask(__name__)
api = Api(app)

api.add_resource(AddUser, "/api/user_add")
api.add_resource(AddLecture, "/api/lecture_add")
api.add_resource(FindMost, "/api/find_most")
api.add_resource(FindAll, "/api/find_all")
api.add_resource(GetSpecialization, "/api/get_specialization")
api.add_resource(GetObject, "/api/get_object")

if __name__ == "__main__":
    app.run(debug=True, port=3000, host="127.0.0.1")
