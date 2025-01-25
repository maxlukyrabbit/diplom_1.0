import requests

url = "http://77.222.47.209:3000/api/user_add"
data = {
    "surname": "Закиров",
    "name": "Ильнур",
    "patronymic": "Ринатович",
    "type_user_id": 3,
    "specialization_id": 1,
    "course": 4,
    "password": "123456789"
}

response = requests.put(url, json=data)
print(response.status_code, response.json())
