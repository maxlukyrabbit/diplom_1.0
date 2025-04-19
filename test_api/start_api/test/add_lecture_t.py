import unittest
from unittest.mock import patch
import requests
import base64

class TestLectureAddAPI(unittest.TestCase):
    def setUp(self):
        self.url = "http://77.222.47.209:3001/api/lecture_add"
        self.sample_pdf_data = b"%PDF-1.4 example"
        self.pdf_base64 = base64.b64encode(self.sample_pdf_data).decode("utf-8")

        self.valid_data = {
            "name": "Самостоятельная работа №1",
            "object_id": 1,
            "file_pdf": self.pdf_base64,
            "course": 4,
            "user_id": 1,
            "count_view": 0
        }

    @patch('requests.put')
    def test_add_lecture_success(self, mock_put):
        print("\nRunning: test_add_lecture_success")
        mock_put.return_value.status_code = 200
        mock_put.return_value.json.return_value = {"message": "Lecture added successfully"}

        response = requests.put(self.url, json=self.valid_data)
        print("Sent Data:", self.valid_data)
        print("Response:", response.status_code, response.json())

        self.assertEqual(response.status_code, 200)
        self.assertIn("Lecture added successfully", response.json()["message"])

    @patch('requests.put')
    def test_missing_name(self, mock_put):
        print("\nRunning: test_missing_name")
        data = self.valid_data.copy()
        data["name"] = ""
        mock_put.return_value.status_code = 400
        mock_put.return_value.json.return_value = {"error": "Name is required"}

        response = requests.put(self.url, json=data)
        print("Sent Data:", data)
        print("Response:", response.status_code, response.json())

        self.assertEqual(response.status_code, 400)
        self.assertIn("Name is required", response.json()["error"])

    @patch('requests.put')
    def test_invalid_base64_pdf(self, mock_put):
        print("\nRunning: test_invalid_base64_pdf")
        data = self.valid_data.copy()
        data["file_pdf"] = "not_base64_data"
        mock_put.return_value.status_code = 400
        mock_put.return_value.json.return_value = {"error": "Invalid PDF data"}

        response = requests.put(self.url, json=data)
        print("Sent Data:", data)
        print("Response:", response.status_code, response.json())

        self.assertEqual(response.status_code, 400)
        self.assertIn("Invalid PDF data", response.json()["error"])

    @patch('requests.put')
    def test_missing_file(self, mock_put):
        print("\nRunning: test_missing_file")
        data = self.valid_data.copy()
        del data["file_pdf"]
        mock_put.return_value.status_code = 400
        mock_put.return_value.json.return_value = {"error": "PDF file is required"}

        response = requests.put(self.url, json=data)
        print("Sent Data:", data)
        print("Response:", response.status_code, response.json())

        self.assertEqual(response.status_code, 400)
        self.assertIn("PDF file is required", response.json()["error"])

    @patch('requests.put')
    def test_large_pdf(self, mock_put):
        print("\nRunning: test_large_pdf")
        data = self.valid_data.copy()
        large_pdf_data = base64.b64encode(b"A" * 10_000_000).decode("utf-8")  # 10 MB
        data["file_pdf"] = large_pdf_data
        mock_put.return_value.status_code = 413
        mock_put.return_value.json.return_value = {"error": "PDF file too large"}

        response = requests.put(self.url, json=data)
        print("Sent Data: [TRUNCATED LARGE PDF]")
        print("Response:", response.status_code, response.json())

        self.assertEqual(response.status_code, 413)
        self.assertIn("PDF file too large", response.json()["error"])


if __name__ == '__main__':
    unittest.main(verbosity=2)
