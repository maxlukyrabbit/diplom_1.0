import requests
import base64

url = "http://127.0.0.1:3000/api/lecture_add"

with open("../laba_2_yes.pdf", "rb") as file:
    pdf_data = file.read()

pdf_base64 = base64.b64encode(pdf_data).decode("utf-8")

data = {
    "name": "тестовая лекция",
    "object_id": 1,
    "file_pdf": pdf_base64,
    "course": 4,
    "user_id": 1,
    "count_view": 0
}

response = requests.put(url, json=data)

print(response.status_code, response.json())
