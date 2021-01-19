package cz.hays.primes;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CliApp {

    public static void main(String[] args) {
        if(args.length != 1) {
            System.err.println("Program expects exactly one argument - path to xlsx file.");
            System.exit(1);
        }
        final String xlsxPath = args[0];
        final List<BigInteger> inputValues = readOutPositiveNumbers(xlsxPath);
        final List<BigInteger> primes = filterNonprimes(inputValues);
        primes.forEach(System.out::println);
    }

    private static boolean isPrime(final BigInteger d) {
        final BigInteger two = new BigInteger("2");
        final BigInteger half = d.divide(two);
        for (BigInteger i = two; i.compareTo(half) == -1 ; i = i.add(BigInteger.ONE)) {
            final BigInteger e = d.remainder(i);
            if (d.remainder(i).equals(BigInteger.ZERO)) {
                return false;
            }
        }
        return true;
    }

    private static List<BigInteger> filterNonprimes(final List<BigInteger> inputValues) {
        return inputValues.stream().filter(CliApp::isPrime).collect(Collectors.toList());
    }

    public static List<BigInteger> readOutPositiveNumbers(final String xslxPath)
    {
        final List<BigInteger> result = new ArrayList<>();
        try(final XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(xslxPath))) {
            final XSSFSheet sheet = workbook.getSheetAt(0);

            for(final Row row : sheet) {
                final Cell cell = row.getCell(1);
                if(cell.getCellType() == CellType.STRING) {
                    final String value = cell.getStringCellValue();
                    try {
                        final BigInteger val = new BigInteger(value);
                        if(val.signum() == 1) result.add(val);
                    } catch (final NumberFormatException nfe) {
                        // ignore
                        System.err.printf("Ignoring nonnumeric value '%s'%n", value);
                    }
                }

            }
        } catch (FileNotFoundException e) {
            // we also could log this information somewhere to a log file, but considering the
            // application complexity, suppose logging to stderr is enough
            System.err.printf("Provided file path '%s' does not point to an existing file.\n", xslxPath);
            System.exit(1);
        } catch (IOException e) {
            throw new RuntimeException("There was an error processing the provided input file " + xslxPath, e);
        }
        return result;
    }
}
