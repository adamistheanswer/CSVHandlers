# Didn't end up using this after finding pandas

from sys import argv

with open(argv[1], 'r') as input, open(argv[1].replace('.csv', '') + '_DUP_REM.csv', 'w') as output:
    duplicates = set()
    for row in input:
        if row in duplicates or ',$' in row:
            continue
        duplicates.add(row)
        output.write(row)
