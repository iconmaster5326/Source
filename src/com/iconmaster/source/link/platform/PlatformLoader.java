package com.iconmaster.source.link.platform;

import com.iconmaster.source.link.Linker;
import com.iconmaster.source.link.Platform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author iconmaster
 */
public class PlatformLoader {
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface LoadedPlatform {
		//String name();
	}
	
	public static void loadPlatform(File f) throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;
		
		Method method = sysclass.getDeclaredMethod("addURL", new Class[] {URL.class});
		method.setAccessible(true);
		method.invoke(cl, new Object[] {f.toURI().toURL()});

		ArrayList<String> classNames = new ArrayList<>();
		ZipInputStream zip=new ZipInputStream(new FileInputStream(f));
		for (ZipEntry entry=zip.getNextEntry();entry!=null;entry=zip.getNextEntry()) {
			if(entry.getName().endsWith(".class") && !entry.isDirectory()) {
				StringBuilder className=new StringBuilder();
				for(String part : entry.getName().split("/")) {
					if(className.length() != 0)
						className.append(".");
					className.append(part);
					if(part.endsWith(".class"))
						className.setLength(className.length()-".class".length());
				}
				classNames.add(className.toString());
			}
		}
		
		for (String name : classNames) {
			cl.loadClass(name);
			Class claz = Class.forName(name);
			if (claz.isAnnotationPresent(LoadedPlatform.class)) {
				//System.out.println("Loading "+name);
				Platform p = (Platform) claz.getConstructor(new Class[0]).newInstance();
				//System.out.println(p.name);
				Linker.registerPlatform(p);
			}
		}
	}
}
