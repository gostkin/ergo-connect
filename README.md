# Ergo connect
Provides a simple framework for interatcion with ergo blockchain. 
It gives an ability to compile sigma contract with your node and then submit a transaction (P2S).

# Dependencies
This app requires the following instruments installed:
1. Java 10
2. sbt

Was tested with sbt `1.2.8` and Java `11.0.2`

# Build and run

1. Install the required dependencies
2. Clone the repository: `git clone https://github.com/gostkin/ergo-connect.git`
3. Make sure that you have active internet connection to download all additional required 
scala packages listed in `build.sbt`
4. Run `sbt compile` from project root to compile the sources
5. Run `sbt run <contract.sigma> <transfer_value> <fee> <node_settings.conf` to test the application.
`contract.sigma` is a smart-contract you want to use in transaction (watch [tutorial](https://github.com/ergoplatform/blog/blob/master/content/posts/Ergo_Script_How_To.md)), 
`transfer_value` is amount of coins you want to send, `fee` is miner's 
reward you give for your transaction being included
to the blockchain. `node_settings.conf` is discussed below.

# `node_settings.conf`

Node settings are used to specify the node you want to use for transactions/etc. By default it's meant
that you have set up your local node and it is available at `127.0.0.1:9052` with no `api key`.
Look at the [tutorial](https://github.com/ergoplatform/blog/blob/master/content/posts/Ergo_setup.md) to find out how to set up the node, what is `api key` used for. 

Sample settings:
```
{
    "api_key": "e88524573de35fc8e108814c29bc2bc2dd5f6b3ec9f09c7deed7b47337603d0f",
    "rest_address": "127.0.0.1",
    "rest_port": 9052
}
```