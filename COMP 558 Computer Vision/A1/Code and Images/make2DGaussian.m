function g = make2DGaussian(N, sigma)
% N is odd, and so the origin (0,0) is positioned at indices
% (M+1,M+1) where N = 2*M + 1.
for x = 1:N
    for y = 1:N
        %compute the 2DGaussian
        g(x,y) = exp(-((x-(N+1)/2)^2+(y-(N+1)/2)^2)/(2*sigma^2));
    end
end
%normalize
g = g/sum(sum(g));
end