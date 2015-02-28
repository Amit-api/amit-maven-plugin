package com.amitapi.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.amit.api.compiler.generator.CodeGenerator;
import com.amit.api.compiler.model.Project;
import com.amit.api.compiler.parser.AmitParser;

import java.io.File;
import java.nio.file.Paths;

@Mojo(name = "amit", 
	defaultPhase = LifecyclePhase.GENERATE_SOURCES,
	requiresDependencyResolution = ResolutionScope.COMPILE, 
	requiresProject = true)
public class AmitApiMojo extends AbstractMojo {

	@Parameter(defaultValue = "${basedir}/src/main/amit")
	private File sourceDirectory;
	
	@Parameter
    protected String compileFile;

	@Parameter
    protected String templateJarClass;
	
	@Parameter
    protected String templateUrl;
	
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/amit")
	private File outputDirectory;

	@Parameter(property = "project", required = true, readonly = true)
	protected MavenProject project;
	 
	public void execute() throws MojoExecutionException {
		Log log = getLog();

		if( log.isDebugEnabled() ) {
			log.info("AmitAPI: Output: " + outputDirectory);
			log.info("AmitAPI: Source: " + sourceDirectory);
			log.info("AmitAPI: Template Path: " + templateUrl);
		}	
			
		if ( !sourceDirectory.isDirectory() ) {
			String error = "No AmitAPI files to compile in " + sourceDirectory.getAbsolutePath(); 
			log.error( error );
			throw new MojoExecutionException( error );
		}

		if( !outputDirectory.exists() ) {
			outputDirectory.mkdirs();
		}
		
		if( compileFile == null || compileFile.isEmpty() ) {
			String error = "AmitAPI compileFile is not set";
			log.error( error );
			throw new MojoExecutionException( error );
		}

		if( templateUrl == null || templateUrl.isEmpty() ) {
			String error = "AmitAPI templateUrl is not set";
			log.error( error );
			throw new MojoExecutionException( error );
		}
		
		String pathToFile = Paths.get( sourceDirectory.getAbsolutePath().toString(), compileFile ).toString();
		
		try {
			AmitParser parser = AmitParser.fromFile( pathToFile );
			Project project = parser.parse();
			
			CodeGenerator generator = new CodeGenerator( 
					project, templateJarClass, templateUrl, outputDirectory.toString() );
			
			generator.generate(); 
		} catch( Exception e ) {
			log.error( "compile error", e );
			throw new MojoExecutionException( "Fatal error", e );
		}
	}
}
