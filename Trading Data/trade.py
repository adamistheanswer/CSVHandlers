# Author: Adam Robinson 21/11/18 19:17
# v.2
#
# Input:
# Each row represents a trade.
# The data can be thought of as a time series of values in columns:
#
# <TimeStamp>,<Symbol>,<Quantity>,<Price>
#
# Definitions
# - TimeStamp is value indicating the microseconds since midnight.
# - Symbol is the 3 character unique identifier for a financial
#   instrument (Stock, future etc.)
# - Quantity is the amount traded
# - Price is the price of the trade for that financial instrument.
#
# Safe Assumptions:
# - TimeStamp is always for the same day and won't roll over midnight.
# - TimeStamp is increasing or same as previous tick (time gap will never be < 0).
# - Price - our currency is an integer based currency.  No decimal points.
# - Price - Price is always > 0.

import csv

# Hardcoded CSV Path
csvInput = open('input_data.csv', 'rb')


def parseCSV(input_data):

    Time2Symbol = dict()
    Price2Symbol = dict()
    VolumeOfTrades2Symbol = dict()
    weightedAverageDividend2Symbol = dict()
    weightedAverageDivisor2Symbol = dict()

    line_count = 0
    symbol_count = 0

    with input_data:
        for row in csv.reader(input_data, delimiter=','):
            if row:

                line_count += 1

                # Input CSV Schema <TimeStamp>,<Symbol>,<Quantity>,<Price>

                time = int(row[0])
                symbol = row[1]
                quantity = int(row[2])
                price = int(row[3])

                # Populates list containing times of trades to symbol
                # Time2Symbol = 'aaa': [1, 2],...
                Time2Symbol.setdefault(symbol, [])
                Time2Symbol[symbol].append(time)

                # Aggregates quantity of trades to value at key(symbol)
                # VolumeOfTrades2Symbol = 'aaa': int(Sum of quantity of shares traded),...
                VolumeOfTrades2Symbol.setdefault(symbol, 0)
                VolumeOfTrades2Symbol[symbol] += quantity

                # 20 shares of aaa @ 18
                # 5 shares of aaa @ 7
                # Populates list containing price of trades to symbol
                # Price2Symbol = 'aaa': [18, 7],...
                Price2Symbol.setdefault(symbol, [])
                Price2Symbol[symbol].append(price)

                # Weighted Average Example: the following trades
                #
                # 20 shares of aaa @ 18
                # 5 shares of aaa @ 7
                #
                # Price = ((20 * 18) + (5 * 7)) / (20 + 5) = 15

                # Aggregates running total of Dividends
                # weightedAverageDividend2Symbol = 'aaa': 395,...
                weightedAverageDividend2Symbol.setdefault(symbol, 0)
                weightedAverageDividend2Symbol[symbol] += (quantity * price)

                # Aggregates running total of Divisors
                # weightedAverageDivisor2Symbol = 'aaa': 25,...
                weightedAverageDivisor2Symbol.setdefault(symbol, 0)
                weightedAverageDivisor2Symbol[symbol] += quantity

            else:
                continue

    print('Input  .CSV of ' + str(line_count) + ' Rows Read')

    with open('output.csv', 'w') as outputData:
        writer = csv.writer(outputData, delimiter=',', lineterminator='\n')

        # sorted for output ordering by symbol descending order
        for symbol in sorted(Time2Symbol):

            row = []
            symbol_count += 1

            # Output CSV Schema <symbol>,<MaxTimeGap>,<Volume>,<WeightedAveragePrice>,<MaxPrice>

            # - Symbol
            # Trade identifying symbol
            row.append(symbol)

            # - MaxTimeGap
            # largest time gap between two consecutive trades
            symbolTimeList = Time2Symbol[symbol]
            if len(symbolTimeList) > 1:
                row.append(max([symbolTimeList[n]-symbolTimeList[n-1] for n in range(1, len(symbolTimeList))]))
            else:
                row.append(0)

            # - Volume
            # Sum of shares across all trades to symbol
            row.append(VolumeOfTrades2Symbol[symbol])

            # - WeightedAveragePrice
            # Sum dividends (weighted) for symbol / sum divisors (shares) for symbol
            row.append(int(int(weightedAverageDividend2Symbol[symbol] / int(weightedAverageDivisor2Symbol[symbol]))))

            # - MaxPrice
            # Maximum price across all trades to symbol
            row.append(max(Price2Symbol[symbol]))

            writer.writerow(row)

        # Progress Indicator
        print('Output .CSV of ' + str(symbol_count) + ' Rows Written\n')


parseCSV(csvInput)
