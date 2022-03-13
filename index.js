const {app, BrowserWindow} = require('electron');
const {Client, Intents} = require('discord.js');

const bot = new Client({
  intents: [Intents.FLAGS.GUILDS]
});

let orders = 0;

/**
 * Process online order, type it and stuff.
 * @param {Object} order 
 */
function processOrder(order){
  orders++;
  bot.user.setStatus('Elapsed online orders: ' + orders);
}

bot.on('ready', () => {
  // Set status.
  bot.user.setStatus('Elapsed online orders: ' + orders);
});

app.whenReady().then(() => {
  // Login as the discord bot.
  bot.login('ODQyMTQ4MjIwMDM3NzU5MDI3.YJxFpg.rVXV9FVE8ZSF37mkoLceIrhI8q8'); // DO NOT SHARE
});