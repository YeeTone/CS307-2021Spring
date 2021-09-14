package JsonParse;

import HelpSource.DataFactory;
import HelpSource.DataManipulation;

public class Client {

    public static void main(String[] args) {
        try {
            DataManipulation dm = new DataFactory().createDataManipulation(args[0]);
            dm.openDatasource();
            //dm.addOneMovie("流浪地球;cn;2019;127");
            System.out.println(dm.allContinentNames());
            System.out.println(dm.continentsWithCountryCount());
            System.out.println(dm.FullInformationOfMoviesRuntime(65, 75));
            System.out.println(dm.findMovieById(11));
            dm.closeDatasource();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}

