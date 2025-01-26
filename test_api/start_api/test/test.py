import requests

url = "http://127.0.0.1:3000/api/user_add"

params = {
    "surname": "Закиров",
    "name": "Ильнур"
}

# Send a GET request with query parameters
response = requests.get(url, params=params)

# Print the response status code and JSON response
print(response.status_code, response.json())