import unittest
import filecmp
import os
from mako import parseCSV

current = os.path.dirname(os.path.realpath(__file__))
testData = current + '/TestCases/Test/'
expected = current + '/TestCases/Expected/'


class ParseCSVTest(unittest.TestCase):

    # Test 1:
    # Mako Test Data

    def test_csv_output1(self):
        parseCSV(open(testData + 'test1.csv', 'rb'))
        self.assertTrue(filecmp.cmp('output.csv', expected + 'EX1.csv'))

    # Test 2:
    # Single Symbol
    # Edge case Max Time
    # Edge case Aggregation of Trades Quantity
    # Edge case Weighted Average
    # Edge case Max Price

    def test_csv_output2(self):
        os.remove('output.csv')
        parseCSV(open(testData + 'test2.csv', 'rb'))
        self.assertTrue(filecmp.cmp('output.csv', expected + 'EX2.csv'))

    # Test 3 Singleton:
    # Single CSV Row

    def test_csv_output3(self):
        os.remove('output.csv')
        parseCSV(open(testData + 'test3.csv', 'rb'))
        self.assertTrue(filecmp.cmp('output.csv', expected + 'EX3.csv'))

    # Test 4:
    # Consecutive Values

    def test_csv_output4(self):
        os.remove('output.csv')
        parseCSV(open(testData + 'test4.csv', 'rb'))
        self.assertTrue(filecmp.cmp('output.csv', expected + 'EX4.csv'))

if __name__ == '__main__':
    unittest.main()
