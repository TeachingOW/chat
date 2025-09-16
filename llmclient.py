import socket
import threading
from openai import OpenAI

# Initialize OpenAI client
client = OpenAI()

# Connect to Java server
HOST = "127.0.0.1"   # or the server's IP
PORT = 5000

def receive_messages(sock):
    """Thread function to receive messages from the server."""
    while True:
        try:
            msg = sock.recv(1024).decode()
            if not msg:
                break
            print("Server:", msg)

            # Only respond with GPT if message starts with "LLM:"
            if msg.startswith("LLM:"):
                query = msg[len("LLM:"):].strip()

                response = client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[
                        {"role": "system", "content": "You are a helpful AI client connected to a chat server."},
                        {"role": "user", "content": query}
                    ]
                )

                ai_reply = response.choices[0].message.content.strip()
                print("AI:", ai_reply)

                # Send AI reply back to server
                sock.sendall(ai_reply.encode())

        except Exception as e:
            print("Error:", e)
            break

def main():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.connect((HOST, PORT))
        print("Connected to chat server at", HOST, PORT)

        # Start a background thread to receive & respond
        threading.Thread(target=receive_messages, args=(sock,), daemon=True).start()

        # User can still send normal messages manually
        while True:
            msg = input("You: ")
            sock.sendall(msg.encode())

if __name__ == "__main__":
    main()
