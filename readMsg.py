import discord
import datetime
import time

# Configuration
TARGET_CHANNEL_ID = <INPUT CHANNEL_ID HERE>
TARGET_CHANNEL_ID_TX = <INPUT CHANNEL_ID HERE>
BOT_TOKEN = '<INPUT BOT_TOKEN HERE>'

# Output files
OUTPUT_FILE = 'output.txt'
TIMESTAMP_FILE = 'timestamp.txt'
OUTPUT_TX_FILE = 'outputTx.txt'


class MyClient(discord.Client):
    async def on_ready(self):
        print(f'Logged on as {self.user}!')

    async def on_message(self, message):
        """Process messages from target channels and log them to files."""
        if message.channel.id == TARGET_CHANNEL_ID:
            # Process the message from main channel
            print(f'Message from {message.author}: {message.content}')
            timestamp = int(message.created_at.timestamp())
            
            # Write author and content to output file
            with open(OUTPUT_FILE, 'a') as file:
                print(message.author, file=file)
                print(message.content, file=file)
            
            # Write timestamp to timestamp file
            with open(TIMESTAMP_FILE, 'a') as file2:
                print(timestamp, file=file2)

        if message.channel.id == TARGET_CHANNEL_ID_TX:
            # Process the message from Tx channel
            print(f'Message from {message.author}: {message.content}')
            
            # Write author and content to Tx output file
            with open(OUTPUT_TX_FILE, 'a') as file:
                print(message.author, file=file)
                print(message.content, file=file)


# Bot setup and run
intents = discord.Intents.default()
intents.message_content = True
client = MyClient(intents=intents)
client.run(BOT_TOKEN)