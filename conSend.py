import discord
from discord.ext import commands
import os
import asyncio
import hashlib
import logging
from discord_webhook import DiscordWebhook

# Configuration
CHANNEL_ID = <INPUT CHANNEL_ID HERE>
FILEPATH = "discordM.txt"
FILEPATH2 = "localplayer.txt"
FILE_PATH1 = "ledger_KMC/consensus_HASH.log"
FILE_PATH2 = "localplayer.txt"

# Webhook URLs
WEBHOOK_URL_KMC = "<INPUT WEBHOOK_URL HERE>"

MESSAGE = "File has been modified!"
BOT_TOKEN = '<INPUT BOT_TOKEN HERE>'

# Set up logging
logging.basicConfig(
    filename='file_monitoring.log', 
    level=logging.INFO,
    format='%(asctime)s - %(message)s', 
    datefmt='%Y-%m-%d %H:%M:%S'
)

# Bot setup
intents = discord.Intents.default()
intents.message_content = True
bot = commands.Bot(command_prefix='!', intents=intents)

# Storage for user data
user_directories = {}
user_files = {}
file_hashes = {}


@bot.event
async def on_ready():
    print(f'Bot is ready. Logged in as {bot.user.name}')


@bot.command()
async def set_directory(ctx, directory: str):
    """Set the directory to monitor for the user."""
    user_directories[ctx.author.id] = directory
    await ctx.send(f"Directory set to: {directory}")


@bot.command()
async def select_files(ctx, *files):
    """Select files to monitor in the user's directory."""
    if ctx.author.id not in user_directories:
        await ctx.send("Please set a directory first using !set_directory")
        return

    user_files[ctx.author.id] = files
    await ctx.send(f"Selected files: {', '.join(files)}")


@bot.command()
async def start_monitoring(ctx):
    """Start monitoring the selected files for changes."""
    if ctx.author.id not in user_directories or ctx.author.id not in user_files:
        await ctx.send("Please set a directory and select files first")
        return

    await ctx.send("Starting file monitoring...")
    await monitor_files(ctx.author)


async def monitor_files(user):
    """Monitor files for changes and send notifications via webhook."""
    while True:
        for file in user_files[user.id]:
            full_path = os.path.join(user_directories[user.id], file)
            
            if os.path.exists(full_path):
                try:
                    with open(full_path, 'rb') as f:
                        file_content = f.read()
                        file_hash = hashlib.md5(file_content).hexdigest()

                        if full_path not in file_hashes or file_hashes[full_path] != file_hash:
                            file_hashes[full_path] = file_hash
                            await send_discord_message(WEBHOOK_URL_KMC, MESSAGE)
                except PermissionError:
                    logging.warning(f"Permission denied reading {full_path}, retrying in 5 seconds...")
                    await asyncio.sleep(5)
                    continue
                except Exception as e:
                    logging.error(f"Error reading {full_path}: {e}")
            else:
                logging.warning(f"File not found: {full_path}")

        await asyncio.sleep(1)  # Check every 1 second


async def send_file(user, file_path):
    """Send file to user via DM (currently unused but preserved)."""
    try:
        await user.send(f"File updated: {os.path.basename(file_path)}")
        await user.send(file=discord.File(file_path))
    except discord.errors.Forbidden:
        print(f"Unable to send DM to {user.name}. They may have DMs disabled.")


async def send_discord_message(webhook_url, message):
    """Send message to Discord via webhook with retry logic."""
    max_retries = 2
    retry_delay = 1
    
    for attempt in range(max_retries):
        try:
            file_content = read_file_to_string(FILEPATH)
            await asyncio.sleep(1)
            webhook = DiscordWebhook(url=webhook_url, content=file_content)
            response = webhook.execute()
            
            # If successful, break out of retry loop
            if hasattr(response, 'status_code') and response.status_code == 200:
                return
            elif response:  # If response exists but no status_code attribute
                return
                
        except Exception as e:
            logging.error(f"Webhook attempt {attempt + 1} failed: {e}")
            
        if attempt < max_retries - 1:  # Don't sleep on last attempt
            await asyncio.sleep(retry_delay)
    
    logging.error(f"Failed to send webhook message after {max_retries} attempts")

 
def read_file_to_string(filepath):
    """Read file content and return as string."""
    try:
        with open(filepath, 'r', encoding='utf-8', errors='ignore') as file:
            return file.read()
    except FileNotFoundError:
        print(f"Error: File not found: {filepath}")
        return None
    except Exception as e:
        print(f"An error occurred: {e}")
        return None


def read_two_files(file_path1, file_path2):
    """
    Read the content of two files and return them as strings.

    Args:
        file_path1 (str): Path to first file (ledger_KMC/consensus_HASH.log)
        file_path2 (str): Path to second file (localplayer.txt)

    Returns:
        tuple: Content of both files as strings, or (None, None) if error occurs
    """
    try:
        with open(file_path1, 'r') as file1, open(file_path2, 'r') as file2:
            content_file1 = file1.read()
            content_file2 = file2.read()
        return content_file1, content_file2
    except FileNotFoundError:
        print("Error: One or both files not found.")
        return None, None
    except Exception as e:
        print(f"An error occurred: {e}")
        return None, None


# Clear the log file on startup
open('file_monitoring.log', 'w').close()

# Run the bot
bot.run(BOT_TOKEN)
