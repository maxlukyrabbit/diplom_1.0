import requests
import base64

url = "http://127.0.0.1:3000/api/lecture_add"

params = {
    "id_lecture": 4
}
response = requests.get(url, params=params)
try:
    if response.status_code == 200:
        response_data = response.json()
        if response_data["result"] == 0:
            file_pdf_base64 = response_data["data"]["file_pdf"]
            file_pdf_binary = base64.b64decode(file_pdf_base64)
            with open("GGGG.pdf", "wb") as file:
                file.write(file_pdf_binary)

            print("Файл успешно сохранён как GGGG.pdf")
        else:
            print(f"Ошибка: {response_data.get('message', 'Unknown error')}")
    else:
        print(f"Ошибка: статус ответа {response.status_code}")
except ValueError as e:
    print("Ошибка обработки JSON ответа:", e)
    print("Ответ сервера:", response.text)
