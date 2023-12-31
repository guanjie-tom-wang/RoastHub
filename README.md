# RoastHub
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
- Built below UI in React [Next.js]( https://github.com/guanjie-tom-wang/RoastHub-React)

## License

This project is licensed under the [MIT License](LICENSE).

