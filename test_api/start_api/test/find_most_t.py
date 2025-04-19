import unittest
from unittest.mock import patch
import requests

class TestFindMostAPI(unittest.TestCase):
    def setUp(self):
        self.url = "http://127.0.0.1:3000/api/find_most"

    @patch('requests.get')
    def test_find_most_success(self, mock_get):
        print("\nRunning: test_find_most_success")
        mock_data = {
            "id": 42,
            "title": "Популярная лекция",
            "views": 1234
        }
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = mock_data

        response = requests.get(self.url)
        print("Response Status:", response.status_code)
        print("Response JSON:", response.json())

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), mock_data)

    @patch('requests.get')
    def test_find_most_server_error(self, mock_get):
        print("\nRunning: test_find_most_server_error")
        mock_get.return_value.status_code = 500
        mock_get.return_value.json.return_value = {"error": "Internal Server Error"}

        response = requests.get(self.url)
        print("Response Status:", response.status_code)
        print("Response JSON:", response.json())

        self.assertEqual(response.status_code, 500)
        self.assertIn("Internal Server Error", response.json()["error"])


if __name__ == '__main__':
    unittest.main(verbosity=2)
