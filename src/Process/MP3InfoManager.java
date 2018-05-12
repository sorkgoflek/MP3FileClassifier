package Process;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;


// Lib: https://github.com/mpatric/mp3agic

public class MP3InfoManager {
    public final static boolean PRINT_PROCESS = false;

    Mp3File mMp3File;
    ArrayList<ArtistName> artistNameList;

    public MP3InfoManager() {
        artistNameList = new ArrayList<ArtistName>();
        setArtistNameList("ArtistName.txt");
    }

    boolean openMP3(String path) {
        try {
            mMp3File = new Mp3File(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (mMp3File.hasId3v1Tag() && mMp3File.hasId3v2Tag()) {
            mMp3File.removeId3v1Tag();
        }

        return true;
    }

    String getTrackNumber() {
        String trackNumber = "";
        if (mMp3File != null) {
            if (mMp3File.hasId3v1Tag() && mMp3File.getId3v1Tag() != null) {
                trackNumber = mMp3File.getId3v1Tag().getTrack();
            } else if (mMp3File.hasId3v2Tag() && mMp3File.getId3v2Tag() != null) {
                trackNumber = mMp3File.getId3v2Tag().getTrack();
            }
        }

        return trackNumber;
    }


    String getTitle() {
        String strTitle = "";

        if (mMp3File != null) {
            if (mMp3File.hasId3v1Tag() && mMp3File.getId3v1Tag() != null) {
                strTitle = mMp3File.getId3v1Tag().getTitle();
            } else if (mMp3File.hasId3v2Tag() && mMp3File.getId3v2Tag() != null) {
                strTitle = mMp3File.getId3v2Tag().getTitle();
            }
        }

        return strTitle.trim();
    }

    String getArtist() {
        String strArtist = null;

        if (mMp3File != null) {
            if (mMp3File.hasId3v1Tag() && mMp3File.getId3v1Tag() != null) {
                strArtist = mMp3File.getId3v1Tag().getArtist();
            } else if (mMp3File.hasId3v2Tag() && mMp3File.getId3v2Tag() != null) {
                strArtist = mMp3File.getId3v2Tag().getArtist();
            }
        }

        return strArtist.trim();
    }

    String getAlbum() {
        String strAlbum = null;

        if (mMp3File != null) {
            if (mMp3File.hasId3v1Tag() && mMp3File.getId3v1Tag() != null) {
                strAlbum = mMp3File.getId3v1Tag().getAlbum();
            } else if (mMp3File.hasId3v2Tag() && mMp3File.getId3v2Tag() != null) {
                strAlbum = mMp3File.getId3v2Tag().getAlbum();
            }
        }

        return strAlbum.trim();
    }

    String getRightArtistName(String artist) {
        String rightName = null;

        for (ArtistName an : artistNameList) {
            try {
                if (an.from.equals(artist)) {
                    rightName = new String(an.to);

                    if (PRINT_PROCESS) {
                        System.out.println("----- MATCHED ----");
                        System.out.println(an.to + " -> " + rightName);
                    }

                    break;
                }
            } catch (PatternSyntaxException e) {
            }
        }

        return rightName;
    }

    void setArtistNameList(String file) {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader(new File(file));
        } catch (Exception e) {
            e.printStackTrace();
        }

        bufferedReader = new BufferedReader(fileReader);

        while (true) {
            String tmp = null;
            try {
                tmp = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (tmp == null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }

            String from = tmp.substring(0, tmp.lastIndexOf("@@"));
            String to = tmp.substring(tmp.lastIndexOf("@@") + 2);
            ArtistName an = new ArtistName(from, to);

            artistNameList.add(an);
        }
    }

    public void setMetadata(MP3FileMeta meta, String dest) {
        if (mMp3File == null) {
            return;
        }

        if (mMp3File.hasId3v1Tag() && mMp3File.getId3v1Tag() != null) {
            ID3v1 tag = mMp3File.getId3v1Tag();
            tag.setArtist(meta.artist);
        } else if (mMp3File.hasId3v2Tag() && mMp3File.getId3v2Tag() != null) {
            ID3v2 tag = mMp3File.getId3v2Tag();
            tag.setArtist(meta.artist);
        }

        try {
            mMp3File.save(dest);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotSupportedException e) {
            e.printStackTrace();
        }
    }
}
