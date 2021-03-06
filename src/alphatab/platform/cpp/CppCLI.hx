/*
 * This file is part of alphaTab.
 *
 *  alphaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  alphaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with alphaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package alphatab.platform.cpp;

#if cpp

import alphatab.io.FileOutputStream;
import alphatab.io.OutputStream;
import alphatab.platform.cli.CLI;
import cpp.Sys;
import cpp.Lib;
import cpp.FileSystem;
import cpp.io.FileOutput;
import cpp.io.File;
 
/**
 * A C++ cli implementation
 */
class CppCLI extends CLI
{

    public function new() 
    {
        super();
        args = Sys.args();
        exit = Sys.exit;
        print = Lib.print;
        println = Lib.println;
        fileExists = FileSystem.exists;
        isDirectory = FileSystem.isDirectory;
        openWrite = function(path:String) : OutputStream
        {
            return new FileOutputStream(path);
        }
    }
    
}
#end