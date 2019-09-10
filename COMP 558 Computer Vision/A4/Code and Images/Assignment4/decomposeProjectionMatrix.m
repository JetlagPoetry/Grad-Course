function [K, R, C] = decomposeProjectionMatrix(P)
%  returns - camera C in world (object) coordinates (not camera coordinates)
%          - rotation R from object to camera coordinates
%          - camera intrinsics K

%  Claim:   (P31, P32, P33)  defines
%  a unit vector in the direction of the optical axis
%  Proof:   If  [x y 0]' = P [X Y Z 1]'
%  then  P31 X + P32 Y + P33 Z = 0 and so XYZ projects to a
%  point at infinity in the image plane.  But points at 
%  infinity in the image plane are perpendicular to the optical axis.

%  (Don't do the following.  There is no need for it.
%  First normalize P so that (P31, P32, P33) is a unit vector.
%  P = P / sqrt(P(3,1)*P(3,1) + P(3,2)*P(3,2) + P(3,3)*P(3,3));

%  Initialize the estimate of K.   Then, perform a sequence of
%  rotations about the X,Y,Z axes (called Givens rotations).
%  The idea is to start out with P = K, and so P = K I,  
%  where I is a product of rotations -- see comment below 

K = P(:,1:3);
Q = eye(3);   % 3x3 identity matrix. 

%  ... and then end up with 
%  P = P (Q1 (Q2 (Q3 Q3') Q2') Q1') 
%    = (P Q1 Q2 Q3) (Q1 Q2 Q3)'                     See (*) below.
%    =   K  R                         i.e. take transpose to define R
%
%  where the rotation matrices Qi are designed to set certain
%  elements of P to 0.

% First, rotate about z axis such that it sets K(3,1) to zero

theta = atan2( K(3,1), K(3,2) );
Qz = [cos(theta) sin(theta)  0; -sin(theta) cos(theta) 0; 0 0 1];
K = K*Qz;
Q = Q*Qz;

% Then rotate about x axis so that it sets K(3,2) to zero

theta = atan2( K(3,2), K(3,3) );
Qx = [1 0 0; 0 cos(theta) sin(theta); 0 -sin(theta) cos(theta)];
K = K*Qx;
Q = Q*Qx;

% And finally rotate about z axis so that it sets K(2,1) to zero

theta = atan2( K(2,1), K(2,2) );
Qz = [cos(theta) sin(theta)  0; -sin(theta) cos(theta) 0; 0 0 1];
K = K*Qz;
Q = Q*Qz;

% The diagonal elements of K are supposed to be positive, but we
% haven't guarenteed this.
% If one of them is negative, then multiply that column of
% both the K and Q matrices by -1.   

reflection = diag(sign(diag(K)));
K = K * reflection;
R = (Q * reflection)'; %  Q is a rotation, R is possible rotation and reflection
                       %  Note that we take the transpose here.  See (*)
                       %  above.
                       
% R'  maps camera coordinates to world coordinates, so C is returned in world coordinates                        
C = - R' * (K \ P(:,4));

if (0)
% display(['image center should be at (' num2str(sizeI(1)/2) ...
%    '  ' num2str(sizeI(2)/2) ' )']);%
%
 display('projection matrix P');
 P

 display(['X axis vanishing point is (' num2str(P(1,1)/P(3,1)) ' ,' ...
    num2str(P(2,1)/P(3,1)) ' )'])

 display(['Y axis vanishing point is (' num2str(P(1,2)/P(3,2)) ' ,' ...
    num2str(P(2,2)/P(3,2)) ' )'])

 display(['Z axis vanishing point is (' num2str(P(1,3)/P(3,3)) ' ,' ...
    num2str(P(2,3)/P(3,3)) ' )'])

 display('calibration matrix K');
 K
 display('rotation');
 R
 display('camera center C in object coordinates')
 C = - R' * (K \ P(:,4))

 display(['alpha_x = ' num2str(K(1,1))])
 display(['alpha_y = ' num2str(K(2,2))])
 display(['shear = ' num2str(K(1,2))])
 display(['px = ' num2str(K(1,3))])
 display(['py = ' num2str(K(2,3))])

  display('verifying decomposition')
  PP = K * R * [eye(3) -C]
  P
  assert(sum(sum(abs(P - PP) < 1e-6)) == 12)
end