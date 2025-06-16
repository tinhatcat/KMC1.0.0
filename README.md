This is the KMCoin single-server validation system.

It works through two discord channels and one Minecraft server.

Validators read discord for transactions while reading Minecraft for Blocks.

When a new block is found, each validator builds their ledger locally and sends
the last block with consensus hash to discord.

Validators compare the blocks that are shared and meet consensus if blocks match.

If a validator has a block that does not match consensus, then KMCoin infers the
issue and locally updates their ledger by the information shared through discord.

Players do not need to call the python scripts on the login page. They do not contain
webhook URLs for TheKittyMine server and will not start if ran without this information.
However, players can input their own webhooks, channel ids and bot tokens for testing.

Players will use the "transactions" channel of TheKittyMine discord server to transmit
transactions to validators. This removes the server's ability to compromise information
and improves decentralization.

If you have any questions about the current state of KMC/KMCoin, want to become a
validator for the server, or want to host your own server within the KMCoin network,
please email TheKittyMine@proton.me

       /\             /\
      /#^\           / ^\
     /#/ \\_________/ /*\\
    /#/   \  ~~~~~~~~    \\
   (#           ~~~~       )
  (#   \ -[(0)] --- [(0)]-\ )
 (#                          )
(##    -------  \`/   ----   )   Power to All People.
 (##  ---------      -------)    Never to government...
  ###    ---   \___   --- )      Help those that help you.
    ###                 )        Shared Community Value.
       ####          )           Sic Semper Tyrannis.                       
           ########


           
    TheKittyMine loves you
           
