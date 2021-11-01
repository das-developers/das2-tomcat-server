/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.das2.das2server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jbf
 */
public class Config {
    
    private static final Logger logger= Logger.getLogger("das2server");
    
    public static final String PROP_HOME = "home";
    public static final String PROP_DATA_SET_ROOT= "dataSetRoot";
    public static final String PROP_LOGO= "logo";
    public static final String PROP_ID= "id";
    public static final String PROP_BIN= "bin";
    
     private static java.util.Properties prop = new java.util.Properties();
     
     private static void loadProperties() {
          // get class loader
          ClassLoader loader = Config.class.getClassLoader();
          if(loader==null)
            loader = ClassLoader.getSystemClassLoader();

          // assuming you want to load application.properties located in WEB-INF/classes/conf/
          String propFile = System.getProperty("user.home") + "/das2server.properties";
          File fpropFile= new File(propFile);
          if ( !fpropFile.exists() ) {
              prop.setProperty( PROP_HOME,  System.getProperty("user.home") + "/das2server/" );
              prop.setProperty( PROP_LOGO, "$HOME/identity/logo.png" );
              prop.setProperty( PROP_ID, "$HOME/identity/id.txt" );
              prop.setProperty( PROP_BIN, "$HOME/bin/" );
              prop.setProperty( PROP_DATA_SET_ROOT, "$HOME/dataSetRoot/" );
              try {
                  OutputStream out= new FileOutputStream(propFile);
                  try {
                      prop.store( out, propFile);
                  } catch ( IOException ex ) {
                      logger.log( Level.WARNING, null, ex );
                  } finally {
                      out.close();
                  }
              } catch ( IOException ex ) {
                      logger.log( Level.WARNING, null, ex );
              }
              fpropFile= new File(propFile);
                        
              initizializeHome(loader);

          }
          try {
              InputStream in= new FileInputStream(fpropFile);
              try{
                  prop.load(in);
              } catch(Exception e) {
                  logger.log( Level.WARNING, null, e );
              } finally {
                  in.close();
              }
          } catch ( IOException ex ) {
              logger.log( Level.WARNING, null, ex );
          }
          
          // add properties that are missing.
          if ( !prop.contains( PROP_ID ) ) {
              logger.fine("adding property id");
              prop.setProperty( PROP_ID, "$HOME/identity/id.txt" );
              initId(loader);
          }
          
     }

     static {
        loadProperties();
     }
     
     public static java.util.Properties getProperties() {
         return prop;
     }
     
     /**
      * return the property value, with macros like $HOME resolved.
      * @param key the property name
      * @return the property value
      */
     public static String resolveProperty( String key ) {
         return resolveProperty( key,true );
     }
     
     /**
      * return the property value, with macros like $HOME resolved.
      * @param key
      * @param check if true, do sanity checks on some properties.
      * @return the property value
      */
     public static String resolveProperty( String key, boolean check ) {
         String p= prop.getProperty(key);
         if ( p.contains("$HOME") ) {
             p= p.replaceAll("\\$HOME", prop.getProperty(PROP_HOME) );
         }
         
         if ( check ) {
             // sanity checks
             if ( key.equals( PROP_LOGO ) ) { 
                 File f= new File( p );
                 if ( !f.exists() ) throw new IllegalArgumentException("logo not found at "+p);
            } else if ( key.equals( PROP_DATA_SET_ROOT ) ) {
                p= new File(p).getAbsolutePath().toString();
                File f= new File( p );
                if ( !f.exists() ) throw new IllegalArgumentException("dataSetRoot not found at "+p);
            }
             
         }
         
         return p;
     }
     
     /**
      * transfer in to out.  in and out are closed.
      * @param in the inputStream.
      * @param out the outputStream.
      * @throws IOException 
      */
     public static void transfer( InputStream in, OutputStream out ) throws IOException {
            byte[] buf= new byte[2048];
            try {
                int i= in.read(buf);
                while ( i>-1 ) {
                    out.write(buf,0,i);
                    i= in.read(buf);
                }
            } finally {
                in.close();
                out.close();
            }
     }
     
     private static void initId( ClassLoader loader ) {
        File id= new File( resolveProperty( PROP_ID, false ) );
        InputStream in = loader.getResourceAsStream("/resources/id.txt");
        if ( in==null ) throw new IllegalArgumentException("unable to find id within war file");
        try {
           FileOutputStream out= new FileOutputStream( id );
           transfer( in, out );
        } catch ( IOException ex ) {
           Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }                  
         
     }

     /** 
      * 
      * @param loader used to fetch resources built in to the application.
      */
    private static void initizializeHome( ClassLoader loader ) {
        File home= new File( prop.getProperty( PROP_HOME ) );
        if ( !home.exists() ) {
            home.mkdirs();
            new File( home, "identity" ).mkdirs();
            new File( home, "dataSetRoot" ).mkdirs();
            new File( home, "bin" ).mkdirs();
        }
        
        initId(loader);

        File logo= new File( resolveProperty( PROP_LOGO, false ) );
        InputStream in = loader.getResourceAsStream("/resources/logo.png");
        if ( in==null ) throw new IllegalArgumentException("unable to find logo within war file");
        try {
           FileOutputStream out= new FileOutputStream( logo );
           transfer( in, out );
        } catch ( IOException ex ) {
           Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        File dsdf= new File( resolveProperty( PROP_DATA_SET_ROOT, false ) );
        dsdf= new File( dsdf, "demoAP.dsdf" );
        in = loader.getResourceAsStream("/resources/demoAP.dsdf");
        if ( in==null ) throw new IllegalArgumentException("unable to find dsdf within war file");
        try {
           FileOutputStream out= new FileOutputStream( dsdf );
           transfer( in, out );
        } catch ( IOException ex ) {
           logger.log(Level.SEVERE, null, ex);
        }
        
        String sapUrl= "http://autoplot.org/jnlp/latest/autoplot.jar";
        try {
            URL apUrl= new URL( sapUrl );
            in = apUrl.openStream();
        } catch ( MalformedURLException ex ) {    
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        if ( in==null ) throw new IllegalArgumentException("unable to retrieve autoplot from "+sapUrl );
        try {
           File ap= new File( resolveProperty( PROP_BIN, false ), "autoplot.jar" );
           FileOutputStream out= new FileOutputStream( ap );
           transfer( in, out );
        } catch ( IOException ex ) {
           logger.log(Level.SEVERE, null, ex);
        }
        
        
    }
  }

