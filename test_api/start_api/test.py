import requests

url = "http://127.0.0.1:3000/api/find_most"

response = requests.get(url)

print(response.status_code, response.json())
