import unittest
from unittest.mock import patch
import requests

class TestUserAddAPI(unittest.TestCase):
    def setUp(self):
        self.url = "http://77.222.47.209:3001/api/user_add"
        self.valid_data = {
            "surname": "Николаев",
            "name": "Николай",
            "patronymic": "Николаевич",
            "type_user_id": 3,
            "specialization_id": 1,
            "course": 4,
            "password": "123456789"
        }

    @patch('requests.put')
    def test_add_user_success(self, mock_put):
        print("\nRunning: test_add_user_success")
        mock_put.return_value.status_code = 200
        mock_put.return_value.json.return_value = {"message": "User added successfully"}

        response = requests.put(self.url, json=self.valid_data)
        print("Request Data:", self.valid_data)
        print("Response Status:", response.status_code)
        print("Response JSON:", response.json())

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {"message": "User added successfully"})

    @patch('requests.put')
    def test_add_user_missing_name(self, mock_put):
        print("\nRunning: test_add_user_missing_name")
        data = self.valid_data.copy()
        data["name"] = ""
        mock_put.return_value.status_code = 400
        mock_put.return_value.json.return_value = {"error": "Name is required"}

        response = requests.put(self.url, json=data)
        print("Request Data:", data)
        print("Response Status:", response.status_code)
        print("Response JSON:", response.json())

        self.assertEqual(response.status_code, 400)
        self.assertEqual(response.json()["error"], "Name is required")

    @patch('requests.put')
    def test_add_user_invalid_course_type(self, mock_put):
        print("\nRunning: test_add_user_invalid_course_type")
        data = self.valid_data.copy()
        data["course"] = "fourth"  # string instead of int
        mock_put.return_value.status_code = 400
        mock_put.return_value.json.return_value = {"error": "Course must be an integer"}

        response = requests.put(self.url, json=data)
        print("Request Data:", data)
        print("Response Status:", response.status_code)
        print("Response JSON:", response.json())

        self.assertEqual(response.status_code, 400)
        self.assertIn("Course must be an integer", response.json()["error"])

    @patch('requests.put')
    def test_add_user_missing_password(self, mock_put):
        print("\nRunning: test_add_user_missing_password")
        data = self.valid_data.copy()
        data["password"] = ""
        mock_put.return_value.status_code = 400
        mock_put.return_value.json.return_value = {"error": "Password cannot be empty"}

        response = requests.put(self.url, json=data)
        print("Request Data:", data)
        print("Response Status:", response.status_code)
        print("Response JSON:", response.json())

        self.assertEqual(response.status_code, 400)
        self.assertIn("Password cannot be empty", response.json()["error"])


if __name__ == '__main__':
    unittest.main(verbosity=2)
