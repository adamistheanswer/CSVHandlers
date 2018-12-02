import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class ParseCSV {



    public ArrayList<String> createData() {
        ArrayList<String> result = ReadFile.createData();
        return result;
    }


    public static void main(String[] args) throws IOException, SQLException {

        Connection connection = null;
//        FileInputStream databasePropsInput = null;
////        databasePropsInput = new FileInputStream(new File("C:\\Users\\adam\\Desktop\\DB\\src\\DB"));
//        Properties props = new Properties();
//        props.load(databasePropsInput);
//        databasePropsInput.close();
//        String driver = props.getProperty("jdbc.drivers"
//        String url = "jdbc:postgresql://mod-fund-databases.cs.bham.ac.uk/agr799";
//        String user = props.getProperty("agr799");
//        String password = props.getProperty("qwerty2003");

//        try {
//            Class.forName(driver);
//            System.out.println("PostgreSQL driver registered.");
//        } catch (ClassNotFoundException ex) {
//            System.out.println("Driver not found");
//        }

        connection = DriverManager.getConnection("jdbc:postgresql://mod-fund-databases.cs.bham.ac.uk/agr799", "arg799", "qwerty2003");
        if (connection != null) {
            System.out.println("Connected to Database");
        }
        connection.setAutoCommit(false);

        ParseCSV obj = new ParseCSV();
        ArrayList<String> initialData = obj.createData();
        ArrayList<MusicData> cleanedData = new ArrayList<MusicData>();
        boolean backToSong = false;

        for (int i = 5; i < initialData.size() -5; i++) {

            MusicData entry = new MusicData();
            ArrayList<String> tags = new ArrayList<String>();

            if (Character.isUpperCase(initialData.get(i).charAt(0))) {
                entry.setSong(initialData.get(i));
                i++;
                System.out.println("THE SONG IS: " + entry.getSong());

                entry.setArtist(initialData.get(i));
                i++;
                System.out.println("THE ARTIST IS: " + entry.getArtist());

                entry.setAlbum(initialData.get(i));
                System.out.println("THE ALBUM IS: " + entry.getAlbum());
                i++;

                if (Character.isUpperCase((initialData.get(i)).charAt(0))) {
                    entry.setAlbum(initialData.get(i - 1).concat(initialData.get(i)));
                    i++;
                }
            }

            while (!backToSong) {
                if (!Character.isUpperCase(initialData.get(i).charAt(0))) {
                    System.out.println("A tag is: " + initialData.get(i));
                    entry.addTag(initialData.get(i));
                    i++;
                }
                try {
                    if (Character.isUpperCase(initialData.get(i).charAt(0))) {
                        backToSong = true;
                        i--;
                    }
                } catch (IndexOutOfBoundsException e) {
                    backToSong = true;
                }
            }
            backToSong = false;
            cleanedData.add(entry);
            System.out.println("an entry was created for :" + entry.getSong());
        }

        PreparedStatement storeNewArtist;
        PreparedStatement storeNewAlbum;
        PreparedStatement storeNewTag;
        PreparedStatement storeNewSong;
        PreparedStatement storeNewSongTag;

        HashMap<String, Integer> artists = new HashMap<String, Integer>();
        HashMap<String, Integer> artistsAlbums = new HashMap<String,Integer>();
        HashMap<String, Integer> songsAlbums = new HashMap<String, Integer>();
        HashMap<String, Integer> tags = new HashMap<String, Integer>();
        HashMap<String, Integer> songTagKeys = new HashMap<String, Integer>();

        for (MusicData md : cleanedData) {
            if (artists.get(md.getArtist()) == null || !artists.containsKey(md.getArtist())) {
                String storeArtist = "INSERT INTO artist (id, artist_name) VALUES(DEFAULT, ?);";
                storeNewArtist = connection.prepareStatement(storeArtist, Statement.RETURN_GENERATED_KEYS);
                storeNewArtist.setString(1, md.getArtist());
                storeNewArtist.executeUpdate();
                System.out.println("Inserting " + md.getArtist() + "into artist table ");
                ResultSet rs = storeNewArtist.getGeneratedKeys();
                rs.next();
                int artistKey = rs.getInt(1);
                System.out.println(artistKey);
                artists.put(md.getArtist(), artistKey);
                rs.close();
            }
            if (artistsAlbums.get(md.getAlbum().concat(md.getArtist())) == null || !artistsAlbums.containsKey(md.getAlbum().concat(md.getArtist()))) {
                String storeAlbum = "INSERT INTO album (id, artist_id, album_name) VALUES(DEFAULT, ?, ?);";
                storeNewAlbum = connection.prepareStatement(storeAlbum, Statement.RETURN_GENERATED_KEYS);
                storeNewAlbum.setInt(1, artists.get(md.getArtist()));
                storeNewAlbum.setString(2, md.getAlbum());
                storeNewAlbum.executeUpdate();
                System.out.println("Inserting " + md.getAlbum() + " into album table ");
                ResultSet rs2 = storeNewAlbum.getGeneratedKeys();
                rs2.next();
                int albumKey = rs2.getInt(1);
                artistsAlbums.put(md.getAlbum().concat(md.getArtist()), albumKey);
                rs2.close();
            }
            if (songsAlbums.get(md.getSong().concat(md.getAlbum())) == null || !songsAlbums.containsKey(md.getSong().concat(md.getAlbum()))) {
                String storeSong = "INSERT INTO song (id, song_name, album_id) VALUES(DEFAULT, ?, ?);";
                storeNewSong = connection.prepareStatement(storeSong, Statement.RETURN_GENERATED_KEYS);
                storeNewSong.setString(1, md.getSong());
                storeNewSong.setInt(2, artistsAlbums.get(md.getAlbum().concat(md.getArtist())));
                storeNewSong.executeUpdate();
                System.out.println("Inserting " + md.getSong() + " into song table ");
                ResultSet rs3 = storeNewSong.getGeneratedKeys();
                rs3.next();
                int songKey = rs3.getInt(1);
                songsAlbums.put(md.getSong().concat(md.getAlbum()), songKey);
                rs3.close();
            }
            for (String tag : md.getTags()) {
                if (tags.get(tag) == null || !tags.containsKey(tag)) {
                    String storeTag = "INSERT INTO tag (id, tag_text) VALUES(DEFAULT, ?);";
                    storeNewTag = connection.prepareStatement(storeTag, Statement.RETURN_GENERATED_KEYS);
                    storeNewTag.setString(1, tag);
                    storeNewTag.executeUpdate();
                    ResultSet rs4 = storeNewTag.getGeneratedKeys();
                    System.out.println("Inserting " + tag + " into tag table ");
                    rs4.next();
                    int tagKey = rs4.getInt(1);
                    tags.put(tag, tagKey);
                    rs4.close();
                }
            }
        }
        for (MusicData md: cleanedData) {
            for (String tag : md.getTags()) {
                String storeSongTag = "INSERT INTO song_tag (id, song_id, tag_id) VALUES(DEFAULT, ?, ?)";
                storeNewSongTag = connection.prepareStatement(storeSongTag, Statement.RETURN_GENERATED_KEYS);
                System.out.println("song id: " + songsAlbums.get(md.getSong().concat(md.getAlbum())));
                System.out.println("tag_id " + tags.get(tag));
                storeNewSongTag.setInt(1, songsAlbums.get(md.getSong().concat(md.getAlbum())));
                storeNewSongTag.setInt(2, tags.get(tag));
                storeNewSongTag.executeUpdate();
                System.out.println("Inserting into song_tag table");
                ResultSet rs5 = storeNewSongTag.getGeneratedKeys();
                rs5.next();
                int key = rs5.getInt(1);
            }
        }

        connection.commit();

        try {
            connection.close();
            System.out.println("Database connection closed!!");
            } catch (SQLException e) {
                System.out.println("Database connection failed to close!");
                e.printStackTrace();
            }
    }
}
