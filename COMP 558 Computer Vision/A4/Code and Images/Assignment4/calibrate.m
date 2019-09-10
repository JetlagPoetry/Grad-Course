function  [P, K, R, C] =  calibrate(XYZ, xy)

%  Create the data matrix to be used for the least squares.
%  and perform SVD to find matrix P.

%  BEGIN CODE STUB (REPLACE WITH YOUR OWN CODE)
A = zeros(size(XYZ,1)*2,12);
for i=1:size(XYZ,1)
    A(2*i-1,1:12)=[XYZ(i,1),XYZ(i,2),XYZ(i,3),1,0,0,0,0,-xy(i,1)*XYZ(i,1),-xy(i,1)*XYZ(i,2),-xy(i,1)*XYZ(i,3),-xy(i,1)];
    A(2*i,1:12)=[0,0,0,0,XYZ(i,1),XYZ(i,2),XYZ(i,3),1,-xy(i,2)*XYZ(i,1),-xy(i,2)*XYZ(i,2),-xy(i,2)*XYZ(i,3),-xy(i,2)];
end
[U,S,V] = svd(A);
[m,n] = min(max(S));
P = [V(1,n),V(2,n),V(3,n),V(4,n);V(5,n),V(6,n),V(7,n),V(8,n);V(9,n),V(10,n),V(11,n),V(12,n)];
P = P./sum(sum(P.^2));
%  END CODE STUB 
% P = K * R * [eye(3), -C];
[K, R, C] = decomposeProjectionMatrix(P);

