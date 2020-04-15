package com.ttm.gt;

import java.io.*;
import java.util.*;

public class HierarchyConverter {
    LinkedList<String> output = new LinkedList<>();
    int maxlength = 0;

    class Node{
        String org;
        String parent;
        String desc;
        String dataStorage;
        String destDelim;
        boolean isChild = true;
        LinkedList<Node> child;

        public Node(String org, String parent, String desc, String dataStorage) {
            this.org = org;
            this.parent = parent;
            this.desc = desc;
            this.dataStorage = dataStorage;
            this.child = new LinkedList<>();
        }

        public void setDestDelim(String destDelim) {
            this.destDelim = destDelim;
        }

        public void setIsChild(boolean isChild){
            this.isChild = isChild;
        }

        @Override
//        public String toString() {
//            return "Node{" +
//                    "org='" + org + '\'' +
//                    ", parent='" + parent + '\'' +
//                    ", desc='" + desc + '\'' +
//                    ", dataStorage='" + dataStorage + '\'' +
//                    ", child=" + child +
//                    '}';
//        }

        public String toString() {
            if(isChild)
                return org+destDelim+desc+destDelim+dataStorage+destDelim;
            else
                return parent+destDelim+org+destDelim+desc+destDelim+dataStorage+destDelim;
        }
    }

    Node newNode(String[] arg){
        Node temp = new Node(arg[0],arg[1],arg[2],arg[3]);
        return temp;
    }

    public void process( boolean HeaderFlag, boolean CustomHeader, boolean QuotedStrings, String SrcFile,
                         String SrcDelim, String destFile, String destDelim){
        LinkedList<Node> LNode =  readCsv(HeaderFlag,CustomHeader,QuotedStrings,SrcFile,SrcDelim);
        LinkedList<Node> tree  = createTree(LNode);
        convertTreeToHier(tree,destDelim,  "", false);
        generateHiertoCsvFormate(destDelim);
        writeCsv(HeaderFlag, CustomHeader, QuotedStrings, destFile);
    }

    private void generateHiertoCsvFormate(String destDelim) {
        //create header
        int myCount = maxlength/3;
        LinkedList<String> temp = new LinkedList<>();
        String header = "Top";
        for(int i=0; i < myCount; i++){
            header = header +destDelim+ "L"+(i+1)+"_Member";
            header = header +destDelim+ "L"+(i+1)+"_Description";
            header = header +destDelim+ "L"+(i+1)+"_DataStorage";
        }
        temp.add(header);

            for(int i=0; i < output.size(); i++){
                String s = output.get(i);
                long count_delim = s.chars().filter(ch -> ch == destDelim.charAt(0)).count();
                String rem_delim = "";
                for(int j=0; j < maxlength-count_delim; j++){
                    rem_delim += destDelim;
                }
//                output.set(i,s+rem_delim);
                temp.add(s+rem_delim);
            }
            output = temp;
            for(String s : output){
                System.out.println(s);
            }
    }

    private void convertTreeToHier(LinkedList<Node> tree, String destDelim, String parent, boolean isChild) {
        for(Node t : tree){
            t.setDestDelim(destDelim);
            t.setIsChild(isChild);
            String out = parent + t.toString();
            if(!t.child.isEmpty()){
                convertTreeToHier(t.child,destDelim, out, true);
            }else{
                out = out.substring(0,out.length()-1); // remove last delimiter
                long count_delim = out.chars().filter(ch -> ch == destDelim.charAt(0)).count();
                maxlength = maxlength > (int) count_delim ? maxlength : (int) count_delim;
                output.add(out);
            }
        }
    }

    private LinkedList<Node> createTree(LinkedList<Node> lNode ) {
        LinkedList<Node> tree = new LinkedList<Node>();

        for(Node n : lNode){
            boolean isAlone = true;
            isAlone = addTree(n,tree,isAlone);
            if(isAlone){
                tree.add(n);
            }
        }
        return tree;
    }

    private boolean addTree(Node n, LinkedList<Node> tree, boolean isAlone) {
//        boolean isAlone = true;
        if(tree.isEmpty()){
            return true;
        }else{
            for(int i = 0; i < tree.size(); i++){
                Node nodeTree = tree.get(i);
                if(nodeTree.org.equals(n.parent)){
                    nodeTree.child.add(n);
                    isAlone = false;
                    i = tree.size();
                }else if (!nodeTree.child.isEmpty()){
                    isAlone =  addTree(n,nodeTree.child, isAlone);
                }
            }
        }
        return isAlone;
    }

    private LinkedList<Node> readCsv(boolean headerFlag, boolean customHeader, boolean quotedStrings, String srcFile, String srcDelim) {
        LinkedList<Node> LNode = new LinkedList<>();
        BufferedReader br = null;
        String line = "";
        try {
            if(quotedStrings){
                srcDelim=srcDelim+"(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
            }
            //TODO : split with |
            if(srcDelim.equals("|")){
                srcDelim="\\|";
            }
            br = new BufferedReader(new FileReader(srcFile));
            while ((line = br.readLine()) != null) {
                if(!headerFlag){
                    String[] rows = line.split(srcDelim);
                    Node temp = newNode(rows);
                    LNode.add(temp);
                }else{
                    headerFlag = false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return LNode;
    }

    private void writeCsv(boolean headerFlag,boolean customHeader, boolean qoutedStrings, String destFile){
        try {
            FileWriter csvWriter = new FileWriter(destFile);
            for(String row : output){
                csvWriter.append(row);
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length > 0){
            /*
            -HeaderFlag – True/False indicating if target file should have 1st row header
            -Custom Header - User can indicate a custom header to use in place of the column headers in the file
            -QuotedStrings – place field values in quotes
            -SrcFile – source file (.txt/.csv)
            -DestFile – destination file (.txt/.csv)
            -SrcDelim – delimiter of source file
            -destDelim – delimiter of destination file (default to srcFile if none specified)
            */
            boolean HeaderFlag=false;
            boolean CustomHeader=false;
            boolean QuotedStrings=false;
            String SrcFile="";
            String DestFile="";
            String SrcDelim="";
            String destDelim="";
            for(int i=0; i < args.length;i++){
                switch (args[i].toLowerCase()){
                    case "-headerflag":
                        HeaderFlag = Boolean.valueOf(args[i+1]);
                        i++;
                        break;
                    case "-customheader":
                        CustomHeader = Boolean.valueOf(args[i+1]);
                        i++;
                        break;
                    case "-quotedstrings":
                        QuotedStrings = Boolean.valueOf(args[i+1]);
                        i++;
                        break;
                    case "-srcfile":
                        SrcFile = args[i+1];
                        i++;
                        break;
                    case "-destfile":
                        DestFile = args[i+1];
                        i++;
                        break;
                    case "-srcdelim":
                        SrcDelim = args[i+1];
                        i++;
                        break;
                    case "-destdelim":
                        destDelim = args[i+1];
                        i++;
                        break;
                }

            }
            HierarchyConverter hc = new HierarchyConverter();
            hc.process(HeaderFlag,CustomHeader,QuotedStrings,SrcFile,SrcDelim,DestFile,destDelim);
        }
    }
}
