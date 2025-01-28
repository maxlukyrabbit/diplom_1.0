import requests
import base64

url = "http://77.222.47.209:3000/api/lecture_add"

with open("ЛР№1_Тестирование программных продуктов-76d1b7815c6148cea392172e5d683a77.pdf", "rb") as file:
    pdf_data = file.read()

pdf_base64 = base64.b64encode(pdf_data).decode("utf-8")

data = {
    "name": "Самостоятельная работа №1",
    "object_id": 1,
    "file_pdf": pdf_base64,
    "course": 4,
    "user_id": 1,
    "count_view": 0
}

response = requests.put(url, json=data)

print(response.status_code, response.json())
