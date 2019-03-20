# JPMTest
JPM Test - Trading Algo
Input - One .csv file which has comma separated values
    Entity,Buy/Sell,AgreedFx,Currency,InstructionDate,SettlementDate,Units,Price_per_unit
    
Output File 1 - buy
    This file has Buy/Outgoing  sum of all outgoing amount in USD sorted settlement date wise in descending order.
   
Output File 2 - buyUserData
    This file has the rank of each entity (outgoing) with amount in USD sorted settlement date wise in descending order.
    
Output File 3 - sell
    This file has sell/incoming sum of all incoming amount in USD sorted settlement date wise in descending order.
    
Output File 4 - sellUserData
   This file has the rank of each entity (incoming) with amount in USD sorted settlement date wise in descending order.
