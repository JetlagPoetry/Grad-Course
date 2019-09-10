%  Define the keypoints on the  Xi  Yi  Zi.  
%  These were obtained using a tape measure in cm.  
%  X is oblique to the right, Y is up, and Z is oblique to the left.

XYZ = 10 * ...         %  convert from cm to mm
    [ 25  -4        1.8; ...  % upper right corner on top right hole
      25  -23.2     1.8; ...  % lower right corner on top right hole
      5.7  -4       1.8; ...  % upper left corner on top right hole
      5.7  -23.2    1.8; ...  % lower left corner on top right hole      
      1.8  -4      7.8;  ...  % upper right corner of top left hole
      1.8 -23.2    7.8;  ... % lower right corner of top left hole
      1.8   -4     25.2;...  % upper left corner on top left hole
      1.8   -23.2  25.2; ...    % lower left corner on top left hole
      18     -25   10.3;  ...  % right corner of yellow sticky
      18     -25   18;  ...  % bottom corner of yellow sticky
      10.3   -25   10.3;...  % top corner on yellow sticky
      10.3   -25   18];      % left corner on  yellow sticky

%   To find keypoints, I used the Matlab
%   Data Cursor tool ("x" means column and "y" means row).

%%
%  for corner1.jpg

if (0)
%  
xy1  =   ...
  [2096 651  ; ...  % upper right corner on top right hole
   2067 1852 ; ...  % lower right corner on top right hole
   1554 596  ; ...  % upper left corner on top right hole
   1549 1530 ; ...  % lower left corner on top right hole      
   1197 596   ;  ...  % upper right corner of top left hole
   1204 1506  ;  ... % lower right corner of top left hole
    330  616  ;...  % upper left corner on top left hole
    377 1604 ];    % lower left corner on top left hole[

    %%
%  for corner2.jpg

%  
xy2  =     ...
  [2753  366  ; ...  % upper right corner on top right hole
   2659 1618  ; ...  % lower right corner on top right hole
   2154  309  ; ...  % upper left corner on top right hole
   2112 1266  ; ...  % lower left corner on top right hole      
   1785  304  ;  ...  % upper right corner of top left hole
   1764 1229  ;  ... % lower right corner of top left hole
    922  313  ;...  % upper left corner on top left hole
    948 1297  ];    % lower left corner on top left hole[

end;

%   c1.jpg

xy1  =     ...
  [ 1584 509 ; ...  % upper right corner on top right hole
    1564 1016; ...  % lower right corner on top right hole
    1263 351 ; ...  % upper left corner on top right hole
    1252 826; ...  % lower left corner on top right hole      
    1066 350  ;  ...  % upper right corner of top left hole
    1062 820 ;  ... % lower right corner of top left hole
    661 435  ;...  % upper left corner on top left hole
    672 920 ;    % lower left corner on top left hole
    1248 1041  ;  ...  % right corner of yellow sticky
    1074 1089  ;  ... % bottom  corner of yellow sticky
    1131 965;...  % top  corner on yellow sticky
    957 1012];    %  left corner on  yellow sticky



%  for c2.jpg

xy2  =   ...
  [ 1398 477 ; ...  % upper right corner on top right hole
    1378 984; ...  % lower right corner on top right hole
     1058 318; ...  % upper left corner on top right hole
     1049 796; ...  % lower left corner on top right hole      
      862 319;  ...  % upper right corner of top left hole
      862 791;  ... % lower right corner of top left hole
      454 413;...  % upper left corner on top left hole
      466 908;    % lower left corner on top left hole
      1060 1013;  ...  % right corner of yellow sticky
      885 1068;  ... % bottom  corner of yellow sticky
      944 940;...  % top  corner on yellow sticky
      763 992];    % left corner on  yellow sticky
