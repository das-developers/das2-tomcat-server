
package org.das2.das2server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.ReadableByteChannel;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.das2.datum.DatumRange;
import org.das2.datum.Units;
import org.das2.qstream.filter.ReduceFilter;
import org.autoplot.AutoplotDataServer;
import org.das2.datum.Datum;
import org.das2.qstream.FormatStreamHandler;
import org.das2.qstream.StreamException;
import org.das2.qstream.StreamHandler;
import org.das2.qstream.StreamTool;

/**
 * The Das2Server code.
 * @author jbf
 */
public class Das2Server extends HttpServlet {

    private static final Logger logger= Logger.getLogger("das2server");
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String server= request.getParameter("server");
        if ( server==null ) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                String me= request.getRequestURL().toString();
                        
                out.println("<html><body><h1><b>" 
                        + "<img src=\""+me+"?server=logo\">"
                        + getId() 
                        + "</b>"
                        + "</h1>This is a das2Server. "
                        + "<br>More information about das2 can be found at "
                        + "<a href=\"https://das2.org/\">https://das2.org/</a>."
                        + "<br><br>Bad server keyword.  "
                        + "Server must be [dataset|dsdf|authenticator|groups|compactDataSet|logo|id|list|discovery]\n" 
                        + "<br><a href='"+me+"?server=dsdf&dataset=demoAP.dsdf'>dsdf</a>"
                        +"\n</body></html>");
            } finally {            
                out.close();
            }        
        } else if ( server.equals("list") ) {
            response.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                das2serverList( out );
            }
            
        } else if ( server.equals("logo" ) ) {
            response.setContentType("image/png;charset=UTF-8");
            OutputStream out = response.getOutputStream();
            String logo= Config.resolveProperty(Config.PROP_LOGO);
            InputStream in= new FileInputStream(logo);
            Config.transfer( in, out );
            
        } else if ( server.equals("id" ) ) {
            response.setContentType("text/plain;charset=UTF-8");
            OutputStream out = response.getOutputStream();
            String logo= Config.resolveProperty(Config.PROP_ID);
            InputStream in= new FileInputStream(logo);
            Config.transfer( in, out );
            
        } else if ( server.equals("dsdf" ) ) {  // args: dataset=demoAP.dsdf
            String dataset= request.getParameter("dataset");
            doDsdf( response, dataset );
            
        } else if ( server.equals("dataset") ) {  // args: server=dataset&start_time=2013-03-01T00%3A00%3A00.000Z&end_time=2013-03-02T00%3A00%3A00.000Z&resolution=149.4809688581315&dataset=demoAP.dsdf
            String dataset= request.getParameter("dataset");
            String startTime= request.getParameter("start_time");
            String endTime= request.getParameter("end_time");
            String resolution= request.getParameter("resolution");
            try {
                doDataset( response, dataset, startTime, endTime, resolution );
            } catch ( ParseException | StreamException ex ) {
                throw new ServletException(ex);
            }
            
        }
    }

    /**
     * ensure the id is valid.
     * @param id 
     */
    private void validateDataset( String id ) {
        if ( id==null ) throw new IllegalArgumentException("id is null");
        if ( id.contains("..") ) throw new IllegalArgumentException("id contains ..");
        if ( id.contains("..") ) throw new IllegalArgumentException("id contains ..");
    }
    /**
     * return the string identifying the server.
     * @return
     * @throws IOException 
     */
    private String getId() throws IOException {
        StringBuilder s= new StringBuilder();
        String logo= Config.resolveProperty(Config.PROP_ID);
        InputStream in= new FileInputStream(logo);
        byte[] buf= new byte[140];
        try {
            int i= in.read(buf);
            while ( i>-1 ) {
                s.append( new String( buf, 0, i ) );
                i= in.read(buf);
            }
        } finally {
            in.close();
        }
        return s.toString();
    }
    
    /**
     * 
     * @param out 
     */
    private void das2serverList( PrintWriter out ) {
        Properties p= Config.getProperties();
        String s= Config.resolveProperty( Config.PROP_DATA_SET_ROOT );
        if ( !s.endsWith("/") ) s= s+"/";
        das2serverList( new File(s), s, out );
    }
    
    private void das2serverList( File f, String root, PrintWriter out ) {
        File[] ff= f.listFiles();
        for ( File f1: ff ) {
            if ( f1.isDirectory() ) {
                out.write( f1.toString().substring(root.length()) );
                out.write( "/" ); // very important...
                out.write( "\n" );
                das2serverList( f1, root, out );
            } else {
                out.write( f1.toString().substring(root.length()) );
                out.write( "\n" );
            }
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private Map<String,String> getDsdf( String dataset ) throws IOException {
        InputStream in= new FileInputStream( new File( new File( Config.resolveProperty(Config.PROP_DATA_SET_ROOT ) ), dataset ) );
        BufferedReader read= new BufferedReader( new InputStreamReader(in) );

        Map<String,String> result= new LinkedHashMap();
        
        try {
            String s= read.readLine();
            while ( s!=null ) {
                String[] ss= s.split(";");
                s= ss[0].trim();
                ss= s.split("#");
                s= ss[0].trim();
                if ( s.length()>0 ) {
                    ss= s.split("=",2);
                    String key= ss[0].trim();
                    String value= ss[1].trim();
                    if ( value.startsWith("'") && value.endsWith("'") ) {
                        value= value.substring(1,value.length()-1);
                    }
                    logger.log( Level.FINEST, "{0}={1}", new Object[]{key, value});
                    result.put( key, value );
                }
                s= read.readLine();
            }
        } finally {
            in.close();
        }
        
        return result;
    }
    
    private void doDsdf(HttpServletResponse response, String dataset) throws FileNotFoundException, IOException {
        validateDataset(dataset);

        response.setContentType("text/plain;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        
        StringBuilder sout= new StringBuilder();
        sout.append( "<stream > <properties \n" );
        
        Map<String,String> dsdf= getDsdf(dataset);

        int i=0;
        int n= dsdf.size();
        
        for ( Entry<String,String> ent : dsdf.entrySet() ) {
            String key= ent.getKey();
            String value= ent.getValue();
            sout.append( key ).append("=\"").append(value);
            i++;
            if ( i<n ) sout.append("\"\n"); else sout.append("\" ");
        }
        
        sout.append( "/>\n</stream>" );
        out.write( "[00]".getBytes("US-ASCII") );
        out.write( String.format("%06d",sout.length()).getBytes("US-ASCII") );
        out.write( sout.toString().getBytes("US-ASCII") );
        out.close();
    }

    private void doDataset(HttpServletResponse response, String dataset, String startTime, String endTime, String resolution) throws IOException, ParseException, StreamException {
        Map<String,String> dsdf= getDsdf(dataset);
        
        DatumRange tr;
        try {
            tr= new DatumRange( Units.us2000.parse(startTime), Units.us2000.parse(endTime) );
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
        
        String uri= dsdf.get("uri");
        if ( uri!=null ) {
            
            StreamHandler sh;
            
            Datum dresolution;
            dresolution = Units.seconds.parse(resolution);
                    
            if ( "QReduce".equals( dsdf.get("reducer") ) ) {
                ReduceFilter filter= new ReduceFilter();
                filter.setCadence( dresolution ); // need new jar...
                FormatStreamHandler fsh= new FormatStreamHandler();
                fsh.setOutputStream( response.getOutputStream() );
                filter.setSink( fsh );
                sh= fsh;
            } else {
                FormatStreamHandler fsh= new FormatStreamHandler();
                fsh.setOutputStream( response.getOutputStream() );
                sh= fsh;
            }
            
            try {
                String[] args= new String[] { "--uri", uri, "--timeRange", tr.toString(), "--ascii", "--outfile", "/tmp/das2.server.qds", "--noexit" };
                for ( int i=0; i<args.length; i++ ) {
                    System.err.print( " " +args[i] );
                }
                
                AutoplotDataServer.main( args );
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
            InputStream in= new FileInputStream("/tmp/das2.server.qds" ); //TODO: don't do things this way, this is just to get to stopping point.
            
            StreamTool stin= new StreamTool();

            ReadableByteChannel rin= java.nio.channels.Channels.newChannel( in );

            response.setContentType("text/x-qstream;charset=UTF-8");
            
            stin.readStream( rin, sh );
            
        }
        
    }
}
