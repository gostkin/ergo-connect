# ergo-connect
This app is a simple example of interaction with local node (assumed that local node is connected to mainnet or testnet).
It gives an ability to compile sigma contract with your node and then submit a transaction (P2S). The app takes
path to sigma script, desired transfer value and fee as params. You can build up your own
main app if you wish to use the code differently. Note: a convenient rest api wrapper is 
included to the package, so it will be easy to implement new functionality.