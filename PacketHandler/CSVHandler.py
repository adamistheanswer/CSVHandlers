import os
import glob
import pandas as pd
from sys import argv

# Run -- pyhon ./CSVHandler.py [Output File Name]

# path to raw data and output folder cleaned for data seperation
csvs = []
current = os.path.dirname(os.path.realpath(__file__))
path = current + '/raw_data'
cleaned = current + '/cleaned_data'
allCSV = glob.glob(path + "/*.csv")

# loop through allCSV and append to csvs list
# facility to expand script for tagging GPS data as N/A for earlier collections
for csv in allCSV:
    schema = ['packet', 'timestamp', 'latitude', 'longitude', 'identifier', 'mac', 'vendor', 'SSID']
    DataFrame = pd.read_csv(csv, index_col=None, header=0, names=schema)
    csvs.append(DataFrame)

# concatenate files + Aggressive filtering (Drop row if data incomplete)
DataFrame = pd.DataFrame()
DataFrame = pd.concat(csvs, axis=0, join='outer', join_axes=None).dropna(subset=['packet', 'timestamp', 'latitude', 'longitude', 'identifier', 'mac', 'vendor', 'SSID'])
# remove duplicates
DataFrame.drop_duplicates(subset=None, keep='first', inplace=True)
DataFrame.dropna(subset=['packet', 'timestamp', 'latitude', 'longitude', 'identifier', 'mac', 'vendor', 'SSID'])
# save cleaned to CSV
DataFrame.to_csv(os.path.join(cleaned, argv[1] + '.csv'), header=True, index=False)
