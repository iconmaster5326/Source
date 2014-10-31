package com.iconmaster.source.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iconmaster
 */
public class LibraryLoader {
    private static final Class[] parameters = new Class[] {URL.class};

    public static void addFile(String s) throws IOException
    {
        File f = new File(s);
        addFile(f);
    }

    public static void addFile(File f) throws IOException
    {
		if (!f.exists()) {
			throw new IOException("File "+f+" does not exist at "+f.getAbsolutePath());
		}
        addURL(f.toURI().toURL());
    }

    public static ClassLoader addURL(URL u) throws IOException
    {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] {u});
        } catch (Throwable t) {
            Logger.getLogger(LibraryLoader.class.getName()).log(Level.SEVERE, "error loading {0}", u);
            throw new IOException("Error, could not add URL to system classloader");
        }
		return sysloader;
    }
}
