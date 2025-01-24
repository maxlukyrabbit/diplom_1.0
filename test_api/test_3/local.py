import requests

url = "http://127.0.0.1:3000/api/main/5"
data = {"name": "gg", "videos": 5}

res = requests.put(url, json=data)
print(res.json())