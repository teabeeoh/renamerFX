/*
 * Copyright 2014 Thomas Bolz
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/**
 * Created by Thomas Bolz on 29.12.13.
 */
public class Test {
    String filename1 = "file1.txt";
    String filename2 = "file1.png";
    String filename3 = ".DS_Store";
    String filename4 = "file1png";
    String filename5 = "file1.TXT";
    String filename6 = "file1.PNG";

    public static void main(String[] args) {
        Test test = new Test();
        test.match();
    }

    private void match() {
        String pattern = ".*\\.png|.*\\.txt|\\..*";
        System.out.println(filename1 + "\t" + filename1.matches(pattern));
        System.out.println(filename2 + "\t" + filename2.matches(pattern));
        System.out.println(filename3 + "\t" + filename3.matches(pattern));
        System.out.println(filename4 + "\t" + filename4.matches(pattern));
        System.out.println(filename5 + "\t" + filename5.toLowerCase().matches(pattern));
        System.out.println(filename6 + "\t" + filename6.toLowerCase().matches(pattern));
    }

}
