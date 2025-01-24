import psycopg2
from config import host, user, password, db_name

try:
    connection = psycopg2.connect(
        host=host,
        user=user,
        password=password,
        database=db_name
    )
    connection.autocommit = True

    with connection.cursor() as cursor:
        cursor.execute("SELECT version();")
        print(f"Server version: {cursor.fetchone()}")

    with connection.cursor() as cursor:
        with open("laba_2.pdf", "rb") as file:
            pdf_data = file.read()

            cursor.execute(
                """
                INSERT INTO lecture (
                    name, object_id, file_pdf, course, user_id, count_view
                )
                VALUES (%s, %s, %s, %s, %s, %s)
                """,
                ('laba_2', 1, psycopg2.Binary(pdf_data), 4, 1, 0)
            )

    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT file_pdf 
            FROM lecture 
            WHERE name = %s
            """,
            ('laba_2',)
        )
        result = cursor.fetchone()

        if result and result[0]:
            file_data = result[0]

            with open("laba_2_yes.pdf", "wb") as output_file:
                output_file.write(file_data)




except Exception as _ex:
    print("[INFO] Error:", _ex)

finally:
    if "connection" in locals() and connection:
        connection.close()
        print("[INFO] Connection closed.")
