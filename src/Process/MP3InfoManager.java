package Process;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

/*
 * 앞에 있는 태그의 구조
 * 정보		Length		offset 없음 (길이에 따라 유동적)
 * TAG		4			TPE1, TALB, TPOS ...
 * 공백		3
 * size		1~2			이진수로 표현, 보통 1개
 * 공백		3
 * data		size-1
 *
 * 위의 순서로 여러 가지 Tag와 data가 저장되있음
 * */

/*
 뒤에 있는 태그
 Field      Length    Offsets
 Tag        3           0-2
 Songname   30          3-32
 Artist     30         33-62
 Album      30         63-92
 Year       4          93-96
 Comment    30         97-126
 Genre      1           127
 */

public class MP3InfoManager {
    public final static boolean PRINT_PROCESS = false;

    RandomAccessFile raf;
    ArrayList<ArtistName> artistNameList;

    public MP3InfoManager() {
        artistNameList = new ArrayList<ArtistName>();
        setArtistNameList("ArtistName.txt");
    }

    boolean openMP3(String pathname) {
        try {
            raf = new RandomAccessFile(new File(pathname), "rws");
        } catch (FileNotFoundException ex) {
            return false;
        }

        return true;
    }

    boolean closeMP3() {
        try {
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    String getTrackNumber() {

        String trackNumber = null;

        try {
            raf.seek(21);

            byte b[] = new byte[2];
            raf.read(b);

            trackNumber = new String(b);
        } catch (IOException ex) {
        }

        //
        trackNumber = trackNumber.trim();
        if (trackNumber.charAt(1) == 'T') {
            trackNumber = "0" + trackNumber.substring(0, 1);
        }

        return trackNumber;

    }

    String getTitle30byte() {
        String strTitle = null;

        try {
            raf.seek(raf.length() - 125);

            byte b[] = new byte[30];
            raf.read(b);

            strTitle = new String(b);
        } catch (IOException ex) {
        }

        return strTitle.trim();
    }

    String getTitle() {
        String strTitle = null;

        try {
            raf.seek(0);

            byte b[] = new byte[128];
            raf.read(b);

            String src = new String(b);
            int beginIdx = 45;
            int endIdx = src.indexOf("TPE1");

            strTitle = src.substring(beginIdx, endIdx);
        } catch (IOException ex) {
        }

        return strTitle.trim();
    }

    String getArtist() {
        String strArtist = null;

        try {
            raf.seek(raf.length() - 95);

            byte b[] = new byte[30];
            raf.read(b);

            strArtist = new String(b);
        } catch (IOException ex) {
        }

        return strArtist.trim();
    }

    String getAlbum() {
        String strAlbum = null;

        try {
            raf.seek(raf.length() - 65);

            byte b[] = new byte[30];
            raf.read(b);

            strAlbum = new String(b);
        } catch (IOException ex) {
        }

        return strAlbum.trim();
    }

    String showALL() {
        String all = null;

        try {
            raf.seek(0);

            byte b[] = new byte[128];
            raf.read(b);

            all = new String(b);
        } catch (IOException ex) {
        }

        return all;
    }

    String changeArtistName(String artist) { //이제 이거 안써
        String englishName = null;

        for (ArtistName an : artistNameList) {
            try {
                if (an.from.equals(artist)) {
                    englishName = new String(an.to);
                    /*
                    System.out.println("----- MATCHED ----");
					System.out.println(an.to+" -> "+englishName);
					*/
                    break;
                }
            } catch (PatternSyntaxException e) {
            }
        }

        if (englishName != null) {
            try {
                raf.seek(0);
                byte b[] = new byte[1024];
                raf.read(b);

                String src = new String(b);

                //앞에 있는 태그 수정
                int sizeIdx = src.indexOf("TPE1") + 4 + 3;
                raf.seek(sizeIdx);
                raf.writeBytes(Integer.toHexString(englishName.length() + 1 - 0x30)); //사이즈 수정

                System.out.println("Size: " + (Integer.toHexString(englishName.length() + 1)));

                int artistIdx = sizeIdx + 3;
                raf.seek(artistIdx);
                raf.writeBytes(englishName); //가수 명 수정


                //뒤에 있는 태그 수정
                raf.seek(raf.length() - 95);
                raf.writeBytes(englishName);
            } catch (IOException ex) {
            }
        } else {
            englishName = artist;
        }

        return englishName;
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


    public byte[] getNewFileData(String artist) {
        byte[] data = null;

        //파일 통째로 읽기
        long filesize = 0;
        try {
            filesize = raf.length();
            data = new byte[(int) filesize];
            raf.seek(0);
            raf.readFully(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //블록 사이즈 구하기
        int TPE1idx;
        for (TPE1idx = 0; TPE1idx < filesize - 4; TPE1idx++) {
            if (data[TPE1idx] == 0x54 && data[TPE1idx + 1] == 0x50 && data[TPE1idx + 2] == 0x45 && data[TPE1idx + 3] == 0x31) {//TPE1 == 54 50 45 31
                break;
            }
        }

        int TPE2idx;
        for (TPE2idx = 0; TPE2idx < filesize - 4; TPE2idx++) {
            if (data[TPE2idx] == 0x54 && data[TPE2idx + 1] == 0x50 && data[TPE2idx + 2] == 0x45 && data[TPE2idx + 3] == 0x32) {//TPE2 == 54 50 45 32
                break;
            }
        }
        if (TPE2idx > filesize - 10) { //TPE2가 없다고 판단되면
            TPE2idx = 0;
        }

        byte tmp = 0;
        try {
            raf.seek(TPE1idx + 4 + 3);
            tmp = raf.readByte();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int artistNameSize = Integer.parseInt(Integer.toHexString(tmp & 0xFF), 16);

        int beforeTPESize = 4 + 3 + 1 + 3 + (artistNameSize - 1);
        int newTPESize = 4 + 3 + 1 + 3 + artist.length();


        if (PRINT_PROCESS) {
            System.out.println("artistNameSize: " + artistNameSize);
            System.out.println(Integer.toHexString(tmp & 0xFF));
        }

        //블록 나누기
        byte[] frontBlock = new byte[TPE1idx];
        byte[] TPE1Block = new byte[newTPESize];
        byte[] midBlock = null;
        byte[] TPE2Block = null;
        byte[] backBlock;

        if (TPE2idx == 0) {
            backBlock = new byte[(int) (filesize - beforeTPESize - frontBlock.length)];
        } else {
            midBlock = new byte[(TPE2idx - beforeTPESize - frontBlock.length)];
            TPE2Block = new byte[newTPESize];
            backBlock = new byte[(int) (filesize - beforeTPESize * 2 - frontBlock.length - midBlock.length)];
        }

        System.arraycopy(data, 0, frontBlock, 0, frontBlock.length); //ok
        System.arraycopy(data, frontBlock.length, TPE1Block, 0, newTPESize); //ok

        if (TPE2idx == 0) {
            System.arraycopy(data, beforeTPESize + frontBlock.length, backBlock, 0, backBlock.length); //ok
        } else {
            System.arraycopy(data, beforeTPESize + frontBlock.length, midBlock, 0, midBlock.length);
            System.arraycopy(data, beforeTPESize + frontBlock.length + midBlock.length, TPE2Block, 0, newTPESize);  //
            System.arraycopy(data, beforeTPESize * 2 + frontBlock.length + midBlock.length, backBlock, 0, backBlock.length);
        }

        //확인
        if (PRINT_PROCESS) {
            for (byte b : frontBlock) {
                System.out.print(Integer.toHexString(b & 0xFF) + " ");
            }
            System.out.println("######################");
            for (byte b : TPE1Block) {
                System.out.print(Integer.toHexString(b & 0xFF) + " ");
            }
            System.out.println("######################");
            int i = 0;
            for (byte b : backBlock) {
                if (i == 50) break;
                i++;
                System.out.print(Integer.toHexString(b & 0xFF) + " ");
            }
            System.out.println("######################");
            for (int i2 = 30; i2 > 1; i2--) {
                System.out.print(Integer.toHexString(backBlock[backBlock.length - i2] & 0xFF) + " ");
            }
            //System.exit(0);
        }


        //TPE* 수정
        TPE1Block[7] = (byte) (artist.length() + 1);//������
        System.arraycopy(artist.getBytes(), 0, TPE1Block, 11, artist.length()); //가수 명

        if (TPE2idx != 0) {
            TPE2Block[7] = (byte) (artist.length() + 1);
            System.arraycopy(artist.getBytes(), 0, TPE2Block, 11, artist.length()); //
        }

        if (PRINT_PROCESS) {
            System.out.println(artist.length());
            System.out.println((byte) artist.length());
            System.out.println(Integer.toHexString(TPE1Block[7] & 0xFF));

            for (byte b : TPE1Block) {
                System.out.print(Integer.toHexString(b & 0xFF) + " ");
            }
            for (byte b : TPE2Block) {
                System.out.print(Integer.toHexString(b & 0xFF) + " ");
            }

            //System.exit(0);
        }

        //블록 병합
        byte[] mergedBlock;

        if (TPE2idx == 0) {
            mergedBlock = new byte[(frontBlock.length + TPE1Block.length + backBlock.length)];
        } else {
            mergedBlock = new byte[(frontBlock.length + TPE1Block.length * 2 + midBlock.length + backBlock.length)];
        }

        System.arraycopy(frontBlock, 0, mergedBlock, 0, frontBlock.length);
        System.arraycopy(TPE1Block, 0, mergedBlock, frontBlock.length, TPE1Block.length);

        if (TPE2idx == 0) {
            System.arraycopy(backBlock, 0, mergedBlock, frontBlock.length + TPE1Block.length, backBlock.length);
        } else {
            System.arraycopy(midBlock, 0, mergedBlock, frontBlock.length + TPE1Block.length, midBlock.length);
            System.arraycopy(TPE2Block, 0, mergedBlock, frontBlock.length + TPE1Block.length + midBlock.length, newTPESize);
            System.arraycopy(backBlock, 0, mergedBlock, frontBlock.length + TPE1Block.length * 2 + midBlock.length, backBlock.length);
        }

        if (PRINT_PROCESS) {
            System.out.println("Merged Block: ");
            int j = 0;
            for (byte b : mergedBlock) {
                if (j++ == 200) break;
                System.out.print(Integer.toHexString(b & 0xFF) + " ");
            }
            //System.exit(0);
        }

        return mergedBlock;
    }
}
