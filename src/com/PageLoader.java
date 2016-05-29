package com;

import entrance.HOME;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Created by bobdenos on 27.05.2016.
 * based upon https://github.com/quanla/classreloading
 */
public class PageLoader extends ClassLoader {
    public ArrayList<String> URI = new ArrayList<String>();
    public ArrayList<String> TITLES = new ArrayList<String>();
    public ArrayList<Page> PClass = new ArrayList<Page>();

    private Set<String> loadedClasses = new HashSet<>();
    private Set<String> unavaiClasses = new HashSet<>();
    private LinkedList<F1<String, byte[]>> loaders = new LinkedList<>();
    private ClassLoader parent = PageLoader.class.getClassLoader();

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (loadedClasses.contains(name) || unavaiClasses.contains(name)) {
            return super.loadClass(name); // Use default CL cache
        }

        byte[] newClassData = loadNewClass(name);
        if (newClassData != null) {
            loadedClasses.add(name);
            return loadClass(newClassData, name);
        } else {
            unavaiClasses.add(name);
            return parent.loadClass(name);
        }
    }

    public Class<?> load(String name) {
        try {
            return loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> loadClass(byte[] classData, String name) {
        Class<?> clazz = defineClass(name, classData, 0, classData.length);
        if (clazz != null) {
            if (clazz.getPackage() == null) {
                definePackage(name.replaceAll("\\.\\w+$", ""), null, null, null, null, null, null, null);
            }
            resolveClass(clazz);
        }
        return clazz;
    }

    public static String toFilePath(String name) {
        return name.replaceAll("\\.", "/") + ".class";
    }

    public PageLoader(String source, HOME h){
        this(source);

        Iterator<Path> Pages = h.folder.GetDirFiles("../" + source);
        while(Pages.hasNext()) {
            Page CurrentPage = LoadPage( PathToPageClass(Pages.next()) ,h);
            if(CurrentPage != null) {
                URI.add(CurrentPage.url);
                TITLES.add(CurrentPage.title);
                PClass.add(CurrentPage);
            }
        }
    }

    public PageLoader(String... paths) {
        for (String path : paths) {
            File file = new File(path);

            F1<String, byte[]> loader = loader(file);
            if (loader == null) {
                throw new RuntimeException("Path not exists " + path);
            }
            loaders.add(loader);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public PageLoader(Collection<File> paths) {
        for (File file : paths) {
            F1<String, byte[]> loader = loader(file);
            if (loader == null) {
                throw new RuntimeException("Path not exists " + file.getPath());
            }
            loaders.add(loader);
        }
    }

    public void getPages(List<String> uri, List<String> titles, List<Page> pclass){
        uri = URI;
        titles = TITLES;
        pclass = PClass;
    }

    public static F1<String, byte[]> loader(File file) {
        if (!file.exists()) {
            return null;
        } else if (file.isDirectory()) {
            return dirLoader(file);
        } else {
            try {
                final JarFile jarFile = new JarFile(file);

                return jarLoader(jarFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static File findFile(String filePath, File classPath) {
        File file = new File(classPath, filePath);
        return file.exists() ? file : null;
    }

    public static F1<String, byte[]> dirLoader(final File dir) {
        return filePath -> {
            File file = findFile(filePath, dir);
            if (file == null) {
                return null;
            }

            try {
                return FileToByte(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    private static byte[] FileToByte(InputStream inputStream){
        ByteArrayOutputStream boTemp = null;

        byte[] buffer = null;
        try {
            int read;
            buffer = new byte[8192];
            boTemp = new ByteArrayOutputStream();
            while ((read=inputStream.read(buffer, 0, 8192)) > -1) {
                boTemp.write(buffer, 0, read);
            }
            return boTemp.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static F1<String, byte[]> jarLoader(final JarFile jarFile) {
        return new F1<String, byte[]>() {
            public byte[] e(String filePath) {
                ZipEntry entry = jarFile.getJarEntry(filePath);
                if (entry == null) {
                    return null;
                }
                try {
                    return FileToByte(jarFile.getInputStream(entry));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void finalize() throws Throwable {
                jarFile.close();
                super.finalize();
            }
        };
    }

    protected byte[] loadNewClass(String name) {
        for (F1<String, byte[]> loader : loaders) {
            byte[] data = loader.e(PageLoader.toFilePath(name));
            if (data!= null) {
                return data;
            }
        }
        return null;
    }

    public Page LoadPage(String className, HOME h){
        Class<?> tmpClass = load(className);
        Page page = (Page) newInstance(tmpClass);
        invoke("init",page,h);
        System.out.println(page.title + " Loaded");
        return page;
    }

    public static <A> A newInstance(Class<A> cla){
        try{
            return cla.newInstance();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static <T> T invoke(Method method, Object o, Object... params) {
        try {
            return (T) method.invoke(o, params);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public static Method getMethod(String methodName, Class clazz) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        if (!clazz.equals(Object.class)) {
            Class superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getMethod(methodName, superclass);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static Object invoke(String methodName, Object o, Object... params){
        return invoke(getMethod(methodName,o.getClass()),o,params);
    }

    private static String PathToPageClass(Path PagePath){
        String tmpString = PagePath.toString();
        if(!tmpString.endsWith(".class")) { return ""; }
        return tmpString.substring( tmpString.lastIndexOf('\\')+1, tmpString.length()-6 );
    }

    public interface F1<A, T> {
        /**
         * Evaluate or execute the function
         * @param obj The parameter
         * @return Result of execution
         */
        T e(A obj);
    }
}

