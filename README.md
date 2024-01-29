# [RoastHub](https://roast-hub-react.vercel.app/)
RoastHub is specifically designed to store, manage, and share roasts encountered by users, whether they pertain to things or people.

## Technology Stack

- **Springboot + Spring MVC**: A Java web framework used for building the backend of the website.
- **Docker Compose**: A tool for defining and running multi-container Docker applications.
- **MongoDB**: A NoSQL database for storing dynamic data entered by users.
- **MySQL**: A relational database management system used for storing user information and other structured data.
- **Redis**: In memory database works as a cache to store the runtime documents(pdf, images, etc) and user JWT token to achieve better performance.
- **Mybaties Plus**: reduce the SQL writing to perform MySQL database operations.
- **RabbitMQ** with **Javax Mail** to send a notification email to the user when the user registers.
  
## Features

- User registration and login.
- Input and retrieval of user data.
- Data persistence and secure storage.

## How to Run

1. Clone the repository to your local machine.
2. Start the application using Docker Compose: `docker-compose up`.
3. Build the project in idea.
5. Upload the pdf file for the Breakfast, Lunch, Dinner, and Snack through login/register
6. Access the corresponding information by clicking Breakfast, Lunch, Dinner, and Snack at Nav bar

## FrontEnd 
![image](https://github.com/guanjie-tom-wang/RoastHub-React/assets/80410357/64bb5743-292d-4023-bd11-1dd9f871cebc)
<br>
<img width="1051" alt="image" src="https://github.com/guanjie-tom-wang/RoastHub-React/assets/80410357/49279082-c541-4e2d-8983-622e673cfc69">
<br>
<img width="1051" alt="image" src="https://github.com/guanjie-tom-wang/RoastHub-React/assets/80410357/a9f6f340-59d9-4cf4-bf79-6efae877fe94">
<br>
<img width="1051" alt="image" src="https://github.com/guanjie-tom-wang/RoastHub-React/assets/80410357/dba0cedf-c21a-431c-a5e8-84f342622ed4">
<br>
<img width="1051" alt="image" src="https://github.com/guanjie-tom-wang/RoastHub-React/assets/80410357/64a231c4-027c-44ed-abe0-63afc284b95e">
<be>
<img width="1051" alt="image" src="https://github.com/guanjie-tom-wang/RoastHub-React/assets/80410357/77d687a8-d2d1-43d3-9122-487b6ca2086d">
<be>
<img width="1051" alt="image" src="https://github.com/guanjie-tom-wang/RoastHub-React/assets/80410357/a7a52ac0-0498-4b85-a7a2-5b5940dbc1fb">


## License

This project is licensed under the [MIT License](LICENSE).

