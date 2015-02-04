package com.adamsresearch.jarview;

public class CreateDirectoryTest
{
    public static void main(String args[])
    {
        try
        {
            boolean createdDir = (new java.io.File(args[0]).mkdirs());
            System.out.println("created dir '" + args[0] + "':  " + createdDir);
        }
        catch(Exception exc)
        {
            System.err.println(exc.getClass().getName() + ": '" + exc.getMessage() + "'");
        }
    }
}

